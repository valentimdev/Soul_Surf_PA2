package com.soulsurf.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Mapeia a resposta JSON completa do endpoint 'api.openweathermap.org/data/2.5/weather'.
 */
@Data
@NoArgsConstructor
public class OpenWeatherResponse {

    // Lista de condições climáticas (geralmente só tem um item)
    @JsonProperty("weather")
    private List<WeatherDetail> weather;

    // Dados numéricos principais (temperatura, sensação térmica, etc.)
    @JsonProperty("main")
    private MainData main;

    // Nome da Cidade (campo 'name' no JSON)
    @JsonProperty("name")
    private String name;

    // Código de status de erro (nem sempre está presente, mas é bom ter)
    @JsonProperty("cod")
    private Integer cod;

    // Outros campos comuns, mas que você pode ignorar se não precisar:
    // private Wind wind;
    // private Coord coord;
    // private Long dt;
    // private Integer timezone;


    // --- CLASSES INTERNAS (INNER CLASSES) ---

    /**
     * Contém informações visuais e de descrição do clima.
     */
    @Data
    public static class WeatherDetail {
        private String description; // A descrição do clima (ex: "céu limpo")
        private String icon;        // Código do ícone (ex: "01d")
    }

    /**
     * Contém os dados numéricos principais de temperatura e pressão.
     */
    @Data
    public static class MainData {
        private Double temp;        // Temperatura atual em Celsius (se units=metric)

        // Uso de @JsonProperty para mapear nomes com underscore
        @JsonProperty("feels_like")
        private Double feelsLike;

        @JsonProperty("temp_min")
        private Double tempMin;

        @JsonProperty("temp_max")
        private Double tempMax;

        private Integer pressure;
        private Integer humidity;
    }
}