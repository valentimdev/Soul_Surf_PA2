package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.WeatherDTO;
import com.soulsurf.backend.services.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // Endpoint: GET /api/weather/current?city=Santos,BR
    @GetMapping("/current")
    public ResponseEntity<WeatherDTO> getCurrentWeather(@RequestParam(defaultValue = "Fortaleza,BR") String city) {
        try {
            // Chama o serviço para buscar os dados
            WeatherDTO weather = weatherService.getCurrentWeather(city);
            return ResponseEntity.ok(weather);
        } catch (RuntimeException e) {
            // Captura erros de serviço (ex: cidade não encontrada, API indisponível)
            System.err.println("Erro ao buscar clima: " + e.getMessage());
            // Retorna 503 Service Unavailable ou um 404/400 se for mais específico
            return ResponseEntity.status(503).build();
        }
    }
}