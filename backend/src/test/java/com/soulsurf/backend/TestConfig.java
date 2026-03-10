package com.soulsurf.backend;

import com.soulsurf.backend.core.storage.OracleStorageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    public OracleStorageService oracleStorageService() {
        return mock(OracleStorageService.class);
    }
}