package com.soulsurf.backend.modules.poi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.poi.dto.PointOfInterestDTO;
import com.soulsurf.backend.modules.poi.entity.PoiCategory;
import com.soulsurf.backend.modules.user.controller.LoginRequest;
import com.soulsurf.backend.modules.user.controller.SignupRequest;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class PointOfInterestControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();

        SignupRequest signup = new SignupRequest();
        signup.setEmail("poiuser@example.com");
        signup.setPassword("password123");
        signup.setUsername("poiuser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setEmail("poiuser@example.com");
        login.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.readValue(response, Map.class);
        this.jwtToken = (String) map.get("token");
    }

    @Test
    public void testCreateAndGetPois() throws Exception {
        PointOfInterestDTO dto = new PointOfInterestDTO();
        dto.setNome("Escolinha de Surf");
        dto.setCategoria(PoiCategory.SURF_SCHOOL);
        dto.setLatitude(-3.721);
        dto.setLongitude(-38.521);

        mockMvc.perform(post("/api/pois")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Escolinha de Surf"));

        mockMvc.perform(get("/api/pois"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Escolinha de Surf"));

        mockMvc.perform(get("/api/pois/category/SURF_SCHOOL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Escolinha de Surf"));
    }
}
