package com.soulsurf.backend.modules.weather.controller;

import com.soulsurf.backend.modules.weather.dto.WeatherDTO;
import com.soulsurf.backend.modules.weather.dto.SurfConditionsDTO;
import com.soulsurf.backend.modules.weather.service.SurfConditionsService;
import com.soulsurf.backend.modules.weather.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final SurfConditionsService surfConditionsService;

    public WeatherController(WeatherService weatherService, SurfConditionsService surfConditionsService) {
        this.weatherService = weatherService;
        this.surfConditionsService = surfConditionsService;
    }

    @GetMapping("/current")
    public ResponseEntity<WeatherDTO> getCurrentWeather(@RequestParam(defaultValue = "Fortaleza,BR") String city) {
        WeatherDTO weather = weatherService.getCurrentWeather(city);
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/surf-conditions")
    public ResponseEntity<SurfConditionsDTO> getSurfConditions(
            @RequestParam(defaultValue = "-3.7319") double lat,
            @RequestParam(defaultValue = "-38.5267") double lon,
            @RequestParam(required = false) String beach
    ) {
        SurfConditionsDTO surfConditions = surfConditionsService.getSurfConditions(lat, lon, beach);
        return ResponseEntity.ok(surfConditions);
    }
}

