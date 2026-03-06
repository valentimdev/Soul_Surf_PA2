package com.soulsurf.backend.modules.weather.dto; // DTOs de resposta da sua API

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherDTO {
    private String cityName;
    private double temp;
    private String description;
    private String iconCode;
}

