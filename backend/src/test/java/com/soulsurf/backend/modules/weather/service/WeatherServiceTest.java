package com.soulsurf.backend.modules.weather.service;

import com.soulsurf.backend.modules.weather.dto.OpenWeatherResponse;
import com.soulsurf.backend.modules.weather.dto.WeatherDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.Builder webClientBuilder;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        weatherService = new WeatherService("http://test.com", "testKey", webClientBuilder);
    }

    @Test
    void testGetCurrentWeather() {
        OpenWeatherResponse mockResponse = new OpenWeatherResponse();
        mockResponse.setName("Sao Paulo");

        OpenWeatherResponse.MainData main = new OpenWeatherResponse.MainData();
        main.setTemp(25.0);
        main.setFeelsLike(26.0);
        main.setTempMin(22.0);
        main.setTempMax(28.0);
        main.setHumidity(60);
        main.setPressure(1012);
        mockResponse.setMain(main);

        OpenWeatherResponse.WeatherDetail cond = new OpenWeatherResponse.WeatherDetail();
        cond.setDescription("Clear sky");
        cond.setIcon("01d");
        mockResponse.setWeather(Arrays.asList(cond));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OpenWeatherResponse.class)).thenReturn(Mono.just(mockResponse));

        WeatherDTO result = weatherService.getCurrentWeather("Sao Paulo");

        assertNotNull(result);
        assertEquals(25.0, result.getTemp());
        assertEquals("Clear sky", result.getDescription());
        assertEquals("Sao Paulo", result.getCityName());
    }
}
