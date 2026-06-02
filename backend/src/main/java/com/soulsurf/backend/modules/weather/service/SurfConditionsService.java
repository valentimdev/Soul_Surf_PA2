package com.soulsurf.backend.modules.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.soulsurf.backend.modules.weather.dto.SurfConditionsDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SurfConditionsService {

    private static final String MARINE_CURRENT_FIELDS =
            "wave_height,wave_direction,wave_period,sea_surface_temperature,ocean_current_velocity,ocean_current_direction";
    private static final String WEATHER_CURRENT_FIELDS =
            "wind_speed_10m,wind_direction_10m,wind_gusts_10m,weather_code";

    private static final Pattern FORTALEZA_BULLETIN_PATTERN = Pattern.compile(
            "<a[^>]*href\\s*=\\s*[\"']([^\"']+\\.pdf)[\"'][^>]*>\\s*Boletim\\s+das\\s+Praias\\s+de\\s+Fortaleza\\s*</a>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FALLBACK_BULLETIN_PATTERN = Pattern.compile(
            "(https?://[^\"'\\s>]*Boletim-[^\"'\\s>]*\\.pdf|/wp-content/uploads/[^\"'\\s>]*Boletim-[^\"'\\s>]*\\.pdf)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PERIOD_PATTERN = Pattern.compile(
            "Per(?:i|\\u00ED)odo\\s*:\\s*([^\\r\\n]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BALNEABILITY_LINE_PATTERN = Pattern.compile(
            "^(\\d{1,3}[A-Z]{1,2})\\s*-\\s*(.+?)\\s+(P|I\\*?|A|EA)\\.?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> IMPROPER_STATUS_CODES = Set.of("I", "I*");
    private static final Set<String> ALERT_STATUS_CODES = Set.of("EA");
    private static final Set<String> PROPER_STATUS_CODES = Set.of("P", "A");
    private static final String TIDE_PROVIDER = "Apolo11 - Tabua de Mares";
    private static final String TIDE_STATION = "Fortaleza / CE";
    private static final Duration TIDE_CACHE_TTL = Duration.ofHours(12);
    private static final DateTimeFormatter APOLO_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter APOLO_TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter TIME_LABEL_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Pattern TIDE_ROW_PATTERN = Pattern.compile(
            "<tr>\\s*<td[^>]*>\\s*<font\\s+class=mare_data>\\s*(\\d{2}/\\d{2}/\\d{4}).*?</tr>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern TIDE_EVENT_PATTERN = Pattern.compile(
            "<font\\s+class=mare_nome>\\s*(ALTA|BAIXA)\\s*</font>\\s*<br>\\s*"
                    + "<font\\s+class=mare>\\s*(\\d{1,2})h(\\d{2})\\s*</font>\\s*<br>\\s*"
                    + "<font\\s+class=mare>\\s*([-+]?\\d+(?:[\\.,]\\d+)?)m\\s*</font>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern SUN_WINDOW_PATTERN = Pattern.compile(
            "<b>\\s*Nascente\\s*</b>\\s*<BR>\\s*(\\d{1,2}:\\d{2}).*?"
                    + "<b>\\s*Poente\\s*</b>\\s*<BR>\\s*(\\d{1,2}:\\d{2})",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private final WebClient marineWebClient;
    private final WebClient weatherWebClient;
    private final WebClient webClient;
    private final String semaceBulletinPageUrl;
    private final String tideForecastUrl;
    private final ZoneId tideZoneId;
    private final Object tideCacheLock = new Object();
    private volatile TideForecastSnapshot tideForecastSnapshot;

    public SurfConditionsService(
            @Value("${surf.api.open-meteo.marine-url}") String marineApiUrl,
            @Value("${surf.api.open-meteo.forecast-url}") String forecastApiUrl,
            @Value("${surf.api.semace.bulletin-page-url}") String semaceBulletinPageUrl,
            @Value("${surf.api.apolo11.tide-url}") String tideForecastUrl,
            @Value("${surf.api.tide.timezone:America/Sao_Paulo}") String tideTimezone,
            WebClient.Builder webClientBuilder
    ) {
        this.marineWebClient = webClientBuilder.baseUrl(marineApiUrl).build();
        this.weatherWebClient = webClientBuilder.baseUrl(forecastApiUrl).build();
        this.webClient = webClientBuilder.build();
        this.semaceBulletinPageUrl = semaceBulletinPageUrl;
        this.tideForecastUrl = tideForecastUrl;
        this.tideZoneId = ZoneId.of(tideTimezone);
    }

    @PostConstruct
    void warmTideForecastCache() {
        refreshTideForecastCache("startup");
    }

    @Scheduled(
            cron = "${surf.api.apolo11.tide-refresh-cron:0 20 5 * * *}",
            zone = "${surf.api.tide.timezone:America/Sao_Paulo}"
    )
    public void refreshWeeklyTideForecastCache() {
        refreshTideForecastCache("weekly schedule");
    }

    public SurfConditionsDTO getSurfConditions(double latitude, double longitude, String beachName) {
        validateCoordinates(latitude, longitude);

        JsonNode marineResponse = fetchMarineCurrent(latitude, longitude);
        JsonNode weatherResponse = fetchWeatherCurrent(latitude, longitude);

        SurfConditionsDTO.MarineDTO marine = mapMarine(marineResponse.path("current"));
        SurfConditionsDTO.WindDTO wind = mapWind(weatherResponse.path("current"));
        SurfConditionsDTO.BalneabilityDTO balneability = fetchBalneability(beachName);
        SurfConditionsDTO.TideDTO tide = fetchTideForecast();
        SurfConditionsDTO.SurfQualityDTO surfQuality = evaluateSurfQuality(marine, wind, balneability, tide);

        String timezone = firstNonBlank(
                marineResponse.path("timezone").asText(null),
                weatherResponse.path("timezone").asText(null),
                "GMT"
        );

        return SurfConditionsDTO.builder()
                .requestedAt(Instant.now().toString())
                .location(SurfConditionsDTO.LocationDTO.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .timezone(timezone)
                        .build())
                .marine(marine)
                .wind(wind)
                .surfQuality(surfQuality)
                .balneability(balneability)
                .tide(tide)
                .sources(List.of(
                        "Open-Meteo Marine API",
                        "Open-Meteo Forecast API",
                        "SEMACE - Boletim das Praias de Fortaleza",
                        TIDE_PROVIDER
                ))
                .build();
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude deve estar entre -90 e 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude deve estar entre -180 e 180.");
        }
    }

    private JsonNode fetchMarineCurrent(double latitude, double longitude) {
        JsonNode response = marineWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("timezone", "auto")
                        .queryParam("cell_selection", "sea")
                        .queryParam("current", MARINE_CURRENT_FIELDS)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || response.path("current").isMissingNode()) {
            throw new RuntimeException("Nao foi possivel obter dados marinhos no momento.");
        }
        return response;
    }

    private JsonNode fetchWeatherCurrent(double latitude, double longitude) {
        JsonNode response = weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("timezone", "auto")
                        .queryParam("current", WEATHER_CURRENT_FIELDS)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || response.path("current").isMissingNode()) {
            throw new RuntimeException("Nao foi possivel obter dados de vento no momento.");
        }
        return response;
    }

    private SurfConditionsDTO.MarineDTO mapMarine(JsonNode marineCurrent) {
        return SurfConditionsDTO.MarineDTO.builder()
                .waveHeightMeters(getDouble(marineCurrent, "wave_height"))
                .waveDirectionDegrees(getDouble(marineCurrent, "wave_direction"))
                .wavePeriodSeconds(getDouble(marineCurrent, "wave_period"))
                .seaSurfaceTemperatureC(getDouble(marineCurrent, "sea_surface_temperature"))
                .oceanCurrentVelocityKmh(getDouble(marineCurrent, "ocean_current_velocity"))
                .oceanCurrentDirectionDegrees(getDouble(marineCurrent, "ocean_current_direction"))
                .build();
    }

    private SurfConditionsDTO.WindDTO mapWind(JsonNode weatherCurrent) {
        return SurfConditionsDTO.WindDTO.builder()
                .windSpeedKmh(getDouble(weatherCurrent, "wind_speed_10m"))
                .windDirectionDegrees(getDouble(weatherCurrent, "wind_direction_10m"))
                .windGustKmh(getDouble(weatherCurrent, "wind_gusts_10m"))
                .weatherCode(getInteger(weatherCurrent, "weather_code"))
                .build();
    }

    private SurfConditionsDTO.BalneabilityDTO fetchBalneability(String beachName) {
        try {
            String html = webClient.get()
                    .uri(semaceBulletinPageUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (!StringUtils.hasText(html)) {
                return unavailableBalneability(beachName, "Sem resposta da fonte de balneabilidade.");
            }

            String fortalezaPdfUrl = resolveFortalezaPdfUrl(html);
            if (!StringUtils.hasText(fortalezaPdfUrl)) {
                return unavailableBalneability(beachName, "Boletim de Fortaleza nao foi localizado.");
            }

            String pdfText = extractPdfText(fortalezaPdfUrl);
            List<BalneabilityPoint> points = parseBalneabilityPoints(pdfText);
            String period = extractPeriod(pdfText);

            return buildBalneabilityDto(beachName, fortalezaPdfUrl, period, points);
        } catch (Exception e) {
            log.warn("Falha ao consultar balneabilidade da SEMACE: {}", e.getMessage());
            return unavailableBalneability(beachName, "Nao foi possivel consultar a balneabilidade agora.");
        }
    }

    private SurfConditionsDTO.TideDTO fetchTideForecast() {
        ZonedDateTime now = ZonedDateTime.now(tideZoneId);
        try {
            TideForecastSnapshot snapshot = getTideForecastSnapshot(now);
            return buildTideDto(snapshot.events(), snapshot.sunWindows(), now, snapshot.loadedAt(), snapshot.expiresAt());
        } catch (Exception e) {
            log.warn("Falha ao consultar tabua de mares do Apolo11: {}", e.getMessage());
            return unavailableTide("Nao foi possivel consultar a tabua de mares agora.");
        }
    }

    private TideForecastSnapshot getTideForecastSnapshot(ZonedDateTime now) {
        TideForecastSnapshot snapshot = tideForecastSnapshot;
        if (isTideSnapshotUsable(snapshot, now)) {
            return snapshot;
        }

        synchronized (tideCacheLock) {
            snapshot = tideForecastSnapshot;
            if (isTideSnapshotUsable(snapshot, now)) {
                return snapshot;
            }

            try {
                TideForecastSnapshot refreshedSnapshot = downloadTideForecastSnapshot();
                tideForecastSnapshot = refreshedSnapshot;
                return refreshedSnapshot;
            } catch (Exception e) {
                if (hasTideCoverage(snapshot, now)) {
                    log.warn("Falha ao atualizar tabua de mares; usando cache anterior: {}", e.getMessage());
                    return snapshot;
                }
                throw e;
            }
        }
    }

    private void refreshTideForecastCache(String reason) {
        try {
            TideForecastSnapshot refreshedSnapshot = downloadTideForecastSnapshot();
            synchronized (tideCacheLock) {
                tideForecastSnapshot = refreshedSnapshot;
            }
            log.info("Tabua de mares atualizada por {}. Eventos carregados: {}", reason, refreshedSnapshot.events().size());
        } catch (Exception e) {
            log.warn("Nao foi possivel atualizar a tabua de mares por {}: {}", reason, e.getMessage());
        }
    }

    private TideForecastSnapshot downloadTideForecastSnapshot() {
        String html = webClient.get()
                .uri(tideForecastUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9,en;q=0.8")
                .header("Referer", "https://www.apolo11.com/")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (!StringUtils.hasText(html)) {
            throw new RuntimeException("Sem resposta da tabua de mares.");
        }

        List<TideEvent> events = parseTideEvents(html);
        if (events.isEmpty()) {
            throw new RuntimeException("Nenhum horario de mare foi encontrado na fonte.");
        }

        Instant loadedAt = Instant.now();
        return new TideForecastSnapshot(
                events,
                parseSunWindows(html),
                loadedAt,
                loadedAt.plus(TIDE_CACHE_TTL)
        );
    }

    private boolean isTideSnapshotUsable(TideForecastSnapshot snapshot, ZonedDateTime now) {
        return snapshot != null
                && now.toInstant().isBefore(snapshot.expiresAt())
                && hasTideCoverage(snapshot, now);
    }

    private boolean hasTideCoverage(TideForecastSnapshot snapshot, ZonedDateTime now) {
        return snapshot != null
                && snapshot.events() != null
                && snapshot.events().stream().anyMatch(event -> event.dateTime().isAfter(now));
    }

    SurfConditionsDTO.TideDTO buildTideDtoFromHtml(String html, ZonedDateTime now) {
        List<TideEvent> events = parseTideEvents(html);
        Map<LocalDate, SunWindow> sunWindows = parseSunWindows(html);
        Instant loadedAt = now.toInstant();
        return buildTideDto(events, sunWindows, now, loadedAt, loadedAt.plus(TIDE_CACHE_TTL));
    }

    List<TideEvent> parseTideEvents(String html) {
        List<TideEvent> events = new ArrayList<>();
        if (!StringUtils.hasText(html)) {
            return events;
        }

        Matcher rowMatcher = TIDE_ROW_PATTERN.matcher(html);
        while (rowMatcher.find()) {
            LocalDate date = LocalDate.parse(rowMatcher.group(1), APOLO_DATE_FORMAT);
            String rowHtml = rowMatcher.group(0);
            Matcher eventMatcher = TIDE_EVENT_PATTERN.matcher(rowHtml);

            while (eventMatcher.find()) {
                String type = eventMatcher.group(1).trim().toUpperCase(Locale.ROOT);
                int hour = Integer.parseInt(eventMatcher.group(2));
                int minute = Integer.parseInt(eventMatcher.group(3));
                double heightMeters = Double.parseDouble(eventMatcher.group(4).replace(',', '.'));

                events.add(new TideEvent(
                        type,
                        ZonedDateTime.of(date, LocalTime.of(hour, minute), tideZoneId),
                        heightMeters
                ));
            }
        }

        return events.stream()
                .sorted(Comparator.comparing(TideEvent::dateTime))
                .toList();
    }

    Map<LocalDate, SunWindow> parseSunWindows(String html) {
        if (!StringUtils.hasText(html)) {
            return Map.of();
        }

        Matcher rowMatcher = TIDE_ROW_PATTERN.matcher(html);
        List<SunWindowByDate> windows = new ArrayList<>();
        while (rowMatcher.find()) {
            LocalDate date = LocalDate.parse(rowMatcher.group(1), APOLO_DATE_FORMAT);
            Matcher sunMatcher = SUN_WINDOW_PATTERN.matcher(rowMatcher.group(0));
            if (sunMatcher.find()) {
                windows.add(new SunWindowByDate(
                        date,
                        new SunWindow(
                                LocalTime.parse(sunMatcher.group(1), APOLO_TIME_FORMAT),
                                LocalTime.parse(sunMatcher.group(2), APOLO_TIME_FORMAT)
                        )
                ));
            }
        }

        return windows.stream()
                .collect(Collectors.toMap(
                        SunWindowByDate::date,
                        SunWindowByDate::sunWindow,
                        (first, second) -> first
                ));
    }

    SurfConditionsDTO.TideDTO buildTideDto(
            List<TideEvent> events,
            Map<LocalDate, SunWindow> sunWindows,
            ZonedDateTime now,
            Instant loadedAt,
            Instant expiresAt
    ) {
        if (events == null || events.isEmpty()) {
            return unavailableTide("Nenhum horario de mare foi encontrado na fonte.");
        }

        List<TideEvent> sortedEvents = events.stream()
                .sorted(Comparator.comparing(TideEvent::dateTime))
                .toList();

        TideEvent previousEvent = null;
        TideEvent nextEvent = null;
        for (TideEvent event : sortedEvents) {
            if (!event.dateTime().isAfter(now)) {
                previousEvent = event;
                continue;
            }
            nextEvent = event;
            break;
        }

        List<SurfConditionsDTO.TideSurfWindowDTO> bestWindows = buildBestSurfWindows(sortedEvents, sunWindows, now);
        List<SurfConditionsDTO.TideEventDTO> nextEvents = sortedEvents.stream()
                .filter(event -> event.dateTime().isAfter(now))
                .limit(8)
                .map(this::mapTideEvent)
                .toList();

        if (previousEvent == null || nextEvent == null) {
            return SurfConditionsDTO.TideDTO.builder()
                    .provider(TIDE_PROVIDER)
                    .station(TIDE_STATION)
                    .sourceUrl(tideForecastUrl)
                    .timezone(tideZoneId.toString())
                    .updatedAt(formatInstant(loadedAt))
                    .expiresAt(formatInstant(expiresAt))
                    .currentStatus("INDISPONIVEL")
                    .currentLabel("Mare sem leitura completa")
                    .nextTurnLabel(nextEvent != null ? formatNextTurnLabel(nextEvent) : null)
                    .nextEvent(nextEvent != null ? mapTideEvent(nextEvent) : null)
                    .nextEvents(nextEvents)
                    .bestSurfWindows(bestWindows)
                    .recommendationLabel("Acompanhe a proxima virada")
                    .recommendation("A tabua nao tem evento anterior suficiente para dizer se o mar esta enchendo ou secando agora.")
                    .observation("Horarios em UTC-3, conforme fonte da tabua de mares.")
                    .build();
        }

        String currentStatus = resolveTideStatus(previousEvent, nextEvent);
        double currentHeight = estimateCurrentTideHeight(previousEvent, nextEvent, now);
        Integer fillPercent = estimateFillPercent(previousEvent, nextEvent, currentHeight);
        TideRecommendation recommendation = buildTideRecommendation(currentStatus, fillPercent);

        return SurfConditionsDTO.TideDTO.builder()
                .provider(TIDE_PROVIDER)
                .station(TIDE_STATION)
                .sourceUrl(tideForecastUrl)
                .timezone(tideZoneId.toString())
                .updatedAt(formatInstant(loadedAt))
                .expiresAt(formatInstant(expiresAt))
                .currentStatus(currentStatus)
                .currentLabel(tideStatusLabel(currentStatus))
                .currentHeightMeters(roundOneDecimal(currentHeight))
                .fillPercent(fillPercent)
                .nextTurnLabel(formatNextTurnLabel(nextEvent))
                .previousEvent(mapTideEvent(previousEvent))
                .nextEvent(mapTideEvent(nextEvent))
                .nextEvents(nextEvents)
                .bestSurfWindows(bestWindows)
                .recommendationLabel(recommendation.label())
                .recommendation(recommendation.message())
                .observation("Horarios em UTC-3. A altura da mare e uma previsao astronomica; vento e chuva podem alterar o mar real.")
                .build();
    }

    private List<SurfConditionsDTO.TideSurfWindowDTO> buildBestSurfWindows(
            List<TideEvent> events,
            Map<LocalDate, SunWindow> sunWindows,
            ZonedDateTime now
    ) {
        List<TideWindow> windows = new ArrayList<>();

        for (int index = 0; index < events.size() - 1; index++) {
            TideEvent startEvent = events.get(index);
            TideEvent endEvent = events.get(index + 1);
            if (!"BAIXA".equals(startEvent.type()) || !"ALTA".equals(endEvent.type())) {
                continue;
            }

            Duration segment = Duration.between(startEvent.dateTime(), endEvent.dateTime());
            if (segment.isNegative() || segment.isZero()) {
                continue;
            }

            ZonedDateTime idealStart = startEvent.dateTime().plus(Duration.ofMillis(Math.round(segment.toMillis() * 0.35)));
            ZonedDateTime idealEnd = startEvent.dateTime().plus(Duration.ofMillis(Math.round(segment.toMillis() * 0.75)));
            windows.addAll(clipToDaylight(idealStart, idealEnd, sunWindows));
        }

        return windows.stream()
                .filter(window -> window.endsAt().isAfter(now))
                .sorted(Comparator.comparing(TideWindow::startsAt))
                .limit(4)
                .map(window -> {
                    boolean activeNow = !now.isBefore(window.startsAt()) && !now.isAfter(window.endsAt());
                    return SurfConditionsDTO.TideSurfWindowDTO.builder()
                            .startsAt(window.startsAt().toOffsetDateTime().toString())
                            .endsAt(window.endsAt().toOffsetDateTime().toString())
                            .label(activeNow ? "Boa janela agora" : "Boa janela de surf")
                            .score(activeNow ? 90 : 82)
                            .activeNow(activeNow)
                            .reason("Meia mare enchendo: costuma equilibrar profundidade e formacao da onda em beach breaks.")
                            .build();
                })
                .toList();
    }

    private List<TideWindow> clipToDaylight(
            ZonedDateTime startsAt,
            ZonedDateTime endsAt,
            Map<LocalDate, SunWindow> sunWindows
    ) {
        List<TideWindow> clipped = new ArrayList<>();
        LocalDate date = startsAt.toLocalDate();
        LocalDate lastDate = endsAt.toLocalDate();

        while (!date.isAfter(lastDate)) {
            SunWindow sunWindow = sunWindows.getOrDefault(date, new SunWindow(LocalTime.of(5, 30), LocalTime.of(17, 30)));
            ZonedDateTime dayStart = ZonedDateTime.of(date, sunWindow.sunrise(), tideZoneId);
            ZonedDateTime dayEnd = ZonedDateTime.of(date, sunWindow.sunset(), tideZoneId);
            ZonedDateTime clippedStart = startsAt.isAfter(dayStart) ? startsAt : dayStart;
            ZonedDateTime clippedEnd = endsAt.isBefore(dayEnd) ? endsAt : dayEnd;

            if (clippedEnd.isAfter(clippedStart) && Duration.between(clippedStart, clippedEnd).toMinutes() >= 30) {
                clipped.add(new TideWindow(clippedStart, clippedEnd));
            }

            date = date.plusDays(1);
        }

        return clipped;
    }

    private String resolveTideStatus(TideEvent previousEvent, TideEvent nextEvent) {
        if ("BAIXA".equals(previousEvent.type()) && "ALTA".equals(nextEvent.type())) {
            return "ENCHENDO";
        }
        if ("ALTA".equals(previousEvent.type()) && "BAIXA".equals(nextEvent.type())) {
            return "SECANDO";
        }
        return "VIRANDO";
    }

    private String tideStatusLabel(String status) {
        return switch (status) {
            case "ENCHENDO" -> "Mar enchendo";
            case "SECANDO" -> "Mar secando";
            case "VIRANDO" -> "Mare virando";
            default -> "Mare indisponivel";
        };
    }

    private double estimateCurrentTideHeight(TideEvent previousEvent, TideEvent nextEvent, ZonedDateTime now) {
        long segmentMillis = Duration.between(previousEvent.dateTime(), nextEvent.dateTime()).toMillis();
        if (segmentMillis <= 0) {
            return previousEvent.heightMeters();
        }

        long elapsedMillis = Duration.between(previousEvent.dateTime(), now).toMillis();
        double progress = Math.max(0, Math.min(1, elapsedMillis / (double) segmentMillis));
        double smoothProgress = (1 - Math.cos(Math.PI * progress)) / 2;
        return previousEvent.heightMeters() + ((nextEvent.heightMeters() - previousEvent.heightMeters()) * smoothProgress);
    }

    private Integer estimateFillPercent(TideEvent previousEvent, TideEvent nextEvent, double currentHeight) {
        double lowest = Math.min(previousEvent.heightMeters(), nextEvent.heightMeters());
        double highest = Math.max(previousEvent.heightMeters(), nextEvent.heightMeters());
        double range = highest - lowest;
        if (range <= 0.01) {
            return null;
        }
        int percent = (int) Math.round(((currentHeight - lowest) / range) * 100);
        return Math.max(0, Math.min(100, percent));
    }

    private TideRecommendation buildTideRecommendation(String status, Integer fillPercent) {
        if (fillPercent == null) {
            return new TideRecommendation(
                    "Acompanhe a mare",
                    "Sem altura suficiente para estimar a faixa da mare agora."
            );
        }

        if ("ENCHENDO".equals(status) && fillPercent >= 35 && fillPercent <= 75) {
            return new TideRecommendation(
                    "Boa para surfar agora",
                    "A mare esta enchendo em faixa media, que costuma ser a janela mais segura para beach breaks."
            );
        }

        if ("ENCHENDO".equals(status) && fillPercent >= 20 && fillPercent <= 85) {
            return new TideRecommendation(
                    "Tendendo a melhorar",
                    "A mare esta subindo; procure a meia mare antes de chegar na cheia."
            );
        }

        if (fillPercent <= 15) {
            return new TideRecommendation(
                    "Mare muito baixa",
                    "Pode fechar mais rapido, expor bancos rasos e exigir cautela."
            );
        }

        if (fillPercent >= 90) {
            return new TideRecommendation(
                    "Mare muito cheia",
                    "Pode deixar a onda mais cheia, lenta ou quebrando perto da areia."
            );
        }

        if ("SECANDO".equals(status)) {
            return new TideRecommendation(
                    "Regular pela mare",
                    "A mare esta secando. Pode funcionar, mas tende a perder o empurrao da mare enchendo."
            );
        }

        return new TideRecommendation(
                "Observe no pico",
                "A leitura de mare esta em transicao; confirme visualmente antes de entrar."
        );
    }

    private SurfConditionsDTO.TideEventDTO mapTideEvent(TideEvent event) {
        return SurfConditionsDTO.TideEventDTO.builder()
                .type(event.type())
                .dateTime(event.dateTime().toOffsetDateTime().toString())
                .timeLabel(event.dateTime().format(TIME_LABEL_FORMAT))
                .heightMeters(event.heightMeters())
                .build();
    }

    private String formatNextTurnLabel(TideEvent event) {
        String type = "ALTA".equals(event.type()) ? "alta" : "baixa";
        return "Proxima mare " + type + " as " + event.dateTime().format(TIME_LABEL_FORMAT)
                + " (" + formatHeight(event.heightMeters()) + ")";
    }

    private String formatHeight(double heightMeters) {
        return String.format(Locale.US, "%.2f m", heightMeters);
    }

    private Double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String formatInstant(Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    private SurfConditionsDTO.TideDTO unavailableTide(String reason) {
        Instant now = Instant.now();
        return SurfConditionsDTO.TideDTO.builder()
                .provider(TIDE_PROVIDER)
                .station(TIDE_STATION)
                .sourceUrl(tideForecastUrl)
                .timezone(tideZoneId.toString())
                .updatedAt(now.toString())
                .expiresAt(now.plus(TIDE_CACHE_TTL).toString())
                .currentStatus("INDISPONIVEL")
                .currentLabel("Mare indisponivel")
                .nextEvents(List.of())
                .bestSurfWindows(List.of())
                .recommendationLabel("Sem leitura de mare")
                .recommendation(reason)
                .observation(reason)
                .build();
    }

    private String resolveFortalezaPdfUrl(String html) {
        Matcher fortalezaMatcher = FORTALEZA_BULLETIN_PATTERN.matcher(html);
        if (fortalezaMatcher.find()) {
            return resolveAbsoluteUrl(fortalezaMatcher.group(1));
        }

        Matcher fallbackMatcher = FALLBACK_BULLETIN_PATTERN.matcher(html);
        if (fallbackMatcher.find()) {
            return resolveAbsoluteUrl(fallbackMatcher.group(1));
        }
        return null;
    }

    private String resolveAbsoluteUrl(String href) {
        if (!StringUtils.hasText(href)) {
            return null;
        }
        return URI.create(semaceBulletinPageUrl).resolve(href).toString();
    }

    private String extractPdfText(String pdfUrl) throws Exception {
        byte[] pdfBytes = webClient.get()
                .uri(pdfUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new RuntimeException("Boletim PDF vazio.");
        }

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractPeriod(String pdfText) {
        if (!StringUtils.hasText(pdfText)) {
            return null;
        }
        Matcher matcher = PERIOD_PATTERN.matcher(pdfText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private List<BalneabilityPoint> parseBalneabilityPoints(String pdfText) {
        List<BalneabilityPoint> points = new ArrayList<>();
        if (!StringUtils.hasText(pdfText)) {
            return points;
        }

        String[] lines = pdfText.split("\\R");
        for (String rawLine : lines) {
            String normalizedLine = rawLine.trim().replaceAll("\\s+", " ");
            Matcher matcher = BALNEABILITY_LINE_PATTERN.matcher(normalizedLine);
            if (!matcher.matches()) {
                continue;
            }

            points.add(new BalneabilityPoint(
                    matcher.group(1).trim(),
                    matcher.group(2).trim(),
                    matcher.group(3).trim().toUpperCase(Locale.ROOT)
            ));
        }
        return points;
    }

    private SurfConditionsDTO.BalneabilityDTO buildBalneabilityDto(
            String beachName,
            String reportUrl,
            String period,
            List<BalneabilityPoint> points
    ) {
        String normalizedQuery = normalizeText(beachName);
        boolean hasBeachQuery = StringUtils.hasText(normalizedQuery);

        List<BalneabilityPoint> matchedPoints = hasBeachQuery
                ? points.stream()
                .filter(point -> normalizeText(point.description()).contains(normalizedQuery))
                .toList()
                : points;

        int properCount = (int) points.stream().filter(point -> PROPER_STATUS_CODES.contains(point.statusCode())).count();
        int alertCount = (int) points.stream().filter(point -> ALERT_STATUS_CODES.contains(point.statusCode())).count();
        int improperCount = (int) points.stream().filter(point -> IMPROPER_STATUS_CODES.contains(point.statusCode())).count();

        List<SurfConditionsDTO.BalneabilityPointDTO> mappedPoints = matchedPoints.stream()
                .limit(30)
                .map(point -> SurfConditionsDTO.BalneabilityPointDTO.builder()
                        .pointCode(point.pointCode())
                        .description(point.description())
                        .status(mapStatusLabel(point.statusCode()))
                        .build())
                .toList();

        String overallStatus = resolveOverallStatus(matchedPoints);
        String observation = buildBalneabilityObservation(hasBeachQuery, beachName, points.size(), matchedPoints.size());

        return SurfConditionsDTO.BalneabilityDTO.builder()
                .provider("SEMACE")
                .reportUrl(reportUrl)
                .period(period)
                .beachQuery(beachName)
                .overallStatus(overallStatus)
                .totalPoints(points.size())
                .properPoints(properCount)
                .alertPoints(alertCount)
                .improperPoints(improperCount)
                .matchedPoints(mappedPoints)
                .observation(observation)
                .build();
    }

    private SurfConditionsDTO.BalneabilityDTO unavailableBalneability(String beachName, String reason) {
        return SurfConditionsDTO.BalneabilityDTO.builder()
                .provider("SEMACE")
                .beachQuery(beachName)
                .overallStatus("INDISPONIVEL")
                .totalPoints(0)
                .properPoints(0)
                .alertPoints(0)
                .improperPoints(0)
                .matchedPoints(List.of())
                .observation(reason)
                .build();
    }

    private String resolveOverallStatus(List<BalneabilityPoint> points) {
        if (points.isEmpty()) {
            return "NAO_ENCONTRADO";
        }

        boolean hasImproper = points.stream().anyMatch(point -> IMPROPER_STATUS_CODES.contains(point.statusCode()));
        if (hasImproper) {
            return "IMPROPRIA";
        }

        boolean hasAlert = points.stream().anyMatch(point -> ALERT_STATUS_CODES.contains(point.statusCode()));
        if (hasAlert) {
            return "EM_ALERTA";
        }

        return "PROPRIA";
    }

    private String buildBalneabilityObservation(
            boolean hasBeachQuery,
            String beachName,
            int totalPoints,
            int matchedPoints
    ) {
        if (!hasBeachQuery) {
            return "Resultado geral dos pontos monitorados no boletim semanal de Fortaleza.";
        }
        if (matchedPoints == 0) {
            return "Nenhum ponto foi encontrado para a busca: " + beachName + ".";
        }
        return "Busca por praia retornou " + matchedPoints + " ponto(s) entre " + totalPoints + " monitorados.";
    }

    private String mapStatusLabel(String statusCode) {
        String code = statusCode.toUpperCase(Locale.ROOT);
        return switch (code) {
            case "P" -> "PROPRIA";
            case "A" -> "ACONSELHAVEL";
            case "EA" -> "EM_ALERTA";
            case "I", "I*" -> "IMPROPRIA";
            default -> "DESCONHECIDO";
        };
    }

    private SurfConditionsDTO.SurfQualityDTO evaluateSurfQuality(
            SurfConditionsDTO.MarineDTO marine,
            SurfConditionsDTO.WindDTO wind,
            SurfConditionsDTO.BalneabilityDTO balneability,
            SurfConditionsDTO.TideDTO tide
    ) {
        Double waveHeight = marine.getWaveHeightMeters();
        Double wavePeriod = marine.getWavePeriodSeconds();
        Double windSpeed = wind.getWindSpeedKmh();
        Double windGust = wind.getWindGustKmh();

        if (waveHeight == null && wavePeriod == null && windSpeed == null && windGust == null) {
            return SurfConditionsDTO.SurfQualityDTO.builder()
                    .label("INDISPONIVEL")
                    .score(0)
                    .reasons(List.of("Nao foi possivel calcular a condicao do mar com os dados atuais."))
                    .build();
        }

        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (waveHeight != null) {
            if (waveHeight >= 0.7 && waveHeight <= 2.2) {
                score += 2;
                reasons.add("Altura de onda em faixa surfavel.");
            } else if (waveHeight >= 0.4 && waveHeight <= 3.0) {
                score += 1;
                reasons.add("Altura de onda razoavel.");
            } else {
                score -= 1;
                reasons.add("Altura de onda fora da faixa ideal.");
            }
        }

        if (wavePeriod != null) {
            if (wavePeriod >= 8) {
                score += 2;
                reasons.add("Periodo de onda consistente.");
            } else if (wavePeriod >= 6) {
                score += 1;
                reasons.add("Periodo de onda moderado.");
            } else {
                score -= 1;
                reasons.add("Periodo de onda curto.");
            }
        }

        if (windSpeed != null) {
            if (windSpeed <= 18) {
                score += 2;
                reasons.add("Vento fraco a moderado.");
            } else if (windSpeed <= 28) {
                score += 1;
                reasons.add("Vento intermediario.");
            } else {
                score -= 1;
                reasons.add("Vento forte pode atrapalhar o surf.");
            }
        }

        if (windGust != null && windGust > 35) {
            score -= 1;
            reasons.add("Rajadas fortes no momento.");
        }

        if (balneability != null) {
            String balStatus = balneability.getOverallStatus();
            if ("IMPROPRIA".equalsIgnoreCase(balStatus)) {
                score -= 4;
                reasons.add("Balneabilidade impropria no boletim da SEMACE.");
            } else if ("EM_ALERTA".equalsIgnoreCase(balStatus)) {
                score -= 2;
                reasons.add("Balneabilidade em alerta no boletim da SEMACE.");
            } else if ("PROPRIA".equalsIgnoreCase(balStatus)) {
                score += 1;
                reasons.add("Balneabilidade propria no boletim da SEMACE.");
            }
        }

        if (tide != null && tide.getFillPercent() != null) {
            Integer fillPercent = tide.getFillPercent();
            String tideStatus = tide.getCurrentStatus();

            if ("ENCHENDO".equalsIgnoreCase(tideStatus) && fillPercent >= 35 && fillPercent <= 75) {
                score += 2;
                reasons.add("Mare enchendo em faixa media, boa janela para beach break.");
            } else if ("ENCHENDO".equalsIgnoreCase(tideStatus) && fillPercent >= 20 && fillPercent <= 85) {
                score += 1;
                reasons.add("Mare enchendo tende a ajudar a formacao das ondas.");
            } else if ("SECANDO".equalsIgnoreCase(tideStatus)) {
                reasons.add("Mare secando pode deixar a sessao mais irregular.");
            }

            if (fillPercent <= 15 || fillPercent >= 90) {
                score -= 1;
                reasons.add("Mare em extremo pode piorar a formacao da onda.");
            }
        }

        String label;
        if (score >= 5) {
            label = "BOA";
        } else if (score >= 2) {
            label = "REGULAR";
        } else {
            label = "RUIM";
        }

        return SurfConditionsDTO.SurfQualityDTO.builder()
                .label(label)
                .score(score)
                .reasons(reasons)
                .build();
    }

    private Double getDouble(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.asDouble() : null;
    }

    private Integer getInteger(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isInt() ? value.asInt() : null;
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private record BalneabilityPoint(String pointCode, String description, String statusCode) {
    }

    record TideEvent(String type, ZonedDateTime dateTime, double heightMeters) {
    }

    private record TideForecastSnapshot(
            List<TideEvent> events,
            Map<LocalDate, SunWindow> sunWindows,
            Instant loadedAt,
            Instant expiresAt
    ) {
    }

    private record SunWindow(LocalTime sunrise, LocalTime sunset) {
    }

    private record SunWindowByDate(LocalDate date, SunWindow sunWindow) {
    }

    private record TideWindow(ZonedDateTime startsAt, ZonedDateTime endsAt) {
    }

    private record TideRecommendation(String label, String message) {
    }
}
