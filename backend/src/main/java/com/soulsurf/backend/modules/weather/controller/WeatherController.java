package com.soulsurf.backend.modules.weather.controller;

import com.soulsurf.backend.modules.weather.dto.WeatherDTO;
import com.soulsurf.backend.modules.weather.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public ResponseEntity<WeatherDTO> getCurrentWeather(@RequestParam(defaultValue = "Fortaleza,BR") String city) {
        WeatherDTO weather = weatherService.getCurrentWeather(city);
        return ResponseEntity.ok(weather);
    }
}

