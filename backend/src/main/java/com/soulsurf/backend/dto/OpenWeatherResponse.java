package com.soulsurf.backend.dto; // Sugestão de novo pacote para DTOs de API externa

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class OpenWeatherResponse {

    @JsonProperty("weather")
    private List<WeatherDetail> weather; // Contém descrição e ícone

    @JsonProperty("main")
    private MainData main; // Contém a temperatura

    @JsonProperty("name")
    private String name; // Nome da Cidade

    // --- Sub-classes necessárias para a API externa ---

    @Data
    public static class WeatherDetail {
        private String description; // Ex: "céu limpo"
        private String icon;        // Código do ícone (ex: "01d")
    }

    @Data
    public static class MainData {
        private Double temp;        // Temperatura atual (a que vamos usar)
        @JsonProperty("feels_like")
        private Double feelsLike;   // Sensação térmica
    }
}