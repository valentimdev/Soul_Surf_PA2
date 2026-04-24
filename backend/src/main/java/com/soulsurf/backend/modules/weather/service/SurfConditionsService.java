package com.soulsurf.backend.modules.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.soulsurf.backend.modules.weather.dto.SurfConditionsDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

    private final WebClient marineWebClient;
    private final WebClient weatherWebClient;
    private final WebClient webClient;
    private final String semaceBulletinPageUrl;

    public SurfConditionsService(
            @Value("${surf.api.open-meteo.marine-url}") String marineApiUrl,
            @Value("${surf.api.open-meteo.forecast-url}") String forecastApiUrl,
            @Value("${surf.api.semace.bulletin-page-url}") String semaceBulletinPageUrl,
            WebClient.Builder webClientBuilder
    ) {
        this.marineWebClient = webClientBuilder.baseUrl(marineApiUrl).build();
        this.weatherWebClient = webClientBuilder.baseUrl(forecastApiUrl).build();
        this.webClient = webClientBuilder.build();
        this.semaceBulletinPageUrl = semaceBulletinPageUrl;
    }

    public SurfConditionsDTO getSurfConditions(double latitude, double longitude, String beachName) {
        validateCoordinates(latitude, longitude);

        JsonNode marineResponse = fetchMarineCurrent(latitude, longitude);
        JsonNode weatherResponse = fetchWeatherCurrent(latitude, longitude);

        SurfConditionsDTO.MarineDTO marine = mapMarine(marineResponse.path("current"));
        SurfConditionsDTO.WindDTO wind = mapWind(weatherResponse.path("current"));
        SurfConditionsDTO.BalneabilityDTO balneability = fetchBalneability(beachName);
        SurfConditionsDTO.SurfQualityDTO surfQuality = evaluateSurfQuality(marine, wind, balneability);

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
                .sources(List.of(
                        "Open-Meteo Marine API",
                        "Open-Meteo Forecast API",
                        "SEMACE - Boletim das Praias de Fortaleza"
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
            SurfConditionsDTO.BalneabilityDTO balneability
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
}
