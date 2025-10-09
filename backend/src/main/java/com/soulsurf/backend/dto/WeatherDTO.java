package com.soulsurf.backend.dto; // DTOs de resposta da sua API

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