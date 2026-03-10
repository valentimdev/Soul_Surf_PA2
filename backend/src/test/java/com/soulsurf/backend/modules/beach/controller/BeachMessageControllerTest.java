package com.soulsurf.backend.modules.beach.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
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

public class BeachMessageControllerTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BeachRepository beachRepository;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;
    private Long testBeachId;

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();
        beachRepository.deleteAll();

        Beach beach = new Beach();
        beach.setNome("Praia do Futuro");
        beach.setLocalizacao("CE");
        beach = beachRepository.save(beach);
        this.testBeachId = beach.getId();

        SignupRequest signup = new SignupRequest();
        signup.setEmail("beachmsg@example.com");
        signup.setPassword("password123");
        signup.setUsername("beachmsguser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setEmail("beachmsg@example.com");
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
    public void testListarMensagens() throws Exception {
        mockMvc.perform(get("/api/beaches/" + testBeachId + "/mensagens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testPostarBeachMessage() throws Exception {
        mockMvc.perform(post("/api/beaches/" + testBeachId + "/mensagens")
                .header("Authorization", "Bearer " + jwtToken)
                .param("texto", "Ondas perfeitas hoje!"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.texto").value("Ondas perfeitas hoje!"));
    }
}
