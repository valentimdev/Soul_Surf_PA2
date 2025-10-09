package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.WeatherDTO;
import com.soulsurf.backend.dto.OpenWeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
public class WeatherService {

    private final WebClient webClient;
    private final String apiKey;

    public WeatherService(@Value("${weather.api.url}") String apiUrl,
                          @Value("${weather.api.key}") String apiKey,
                          WebClient.Builder webClientBuilder) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    public WeatherDTO getCurrentWeather(String cityName) {
        String uri = "?q={city}&appid={key}&units=metric&lang=pt";

        OpenWeatherResponse response = this.webClient.get()
                .uri(uri, cityName, apiKey)
                .retrieve()
                .bodyToMono(OpenWeatherResponse.class)
                .block();

        if (response == null || response.getWeather() == null || response.getWeather().isEmpty()) {
            throw new RuntimeException("Dados de clima não disponíveis para " + cityName);
        }

        return WeatherDTO.builder()
                .cityName(response.getName())
                .temp(response.getMain().getTemp())
                .description(response.getWeather().get(0).getDescription())
                .iconCode(response.getWeather().get(0).getIcon())
                .build();
    }
}
