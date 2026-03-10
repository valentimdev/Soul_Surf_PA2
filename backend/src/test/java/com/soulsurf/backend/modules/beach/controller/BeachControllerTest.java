package com.soulsurf.backend.modules.beach.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.user.controller.LoginRequest;
import com.soulsurf.backend.modules.user.controller.SignupRequest;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class BeachControllerTest extends BaseIntegrationTest {

    @Autowired
    private BeachRepository beachRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testBeachId;
    private String jwtToken;

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();
        beachRepository.deleteAll();

        Beach beach = new Beach();
        beach.setNome("Praia de Teste");
        beach.setLocalizacao("SC");
        beach.setDescricao("Uma praia linda");
        beach.setNivelExperiencia("Iniciante");
        beach = beachRepository.save(beach);
        this.testBeachId = beach.getId();

        // Create user and get JWT
        SignupRequest signup = new SignupRequest();
        signup.setEmail("beachuser@example.com");
        signup.setPassword("password123");
        signup.setUsername("beachuser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        // Make user admin for getAllBeachPosts
        User user = userRepository.findByEmail("beachuser@example.com").orElseThrow();
        user.setAdmin(true);
        userRepository.save(user);

        // Create a public post linked to the beach
        Post post = new Post();
        post.setDescricao("Surf at the beach");
        post.setUsuario(user);
        post.setBeach(beach);
        post.setPublico(true);
        postRepository.save(post);

        LoginRequest login = new LoginRequest();
        login.setEmail("beachuser@example.com");
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
    public void testGetAllBeaches() throws Exception {
        mockMvc.perform(get("/api/beaches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Praia de Teste"))
                .andExpect(jsonPath("$[0].localizacao").value("SC"));
    }

    @Test
    public void testGetBeachById() throws Exception {
        mockMvc.perform(get("/api/beaches/" + testBeachId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Praia de Teste"))
                .andExpect(jsonPath("$.localizacao").value("SC"));
    }

    @Test
    public void testCreateBeach() throws Exception {
        mockMvc.perform(multipart("/api/beaches/")
                .header("Authorization", "Bearer " + jwtToken)
                .param("nome", "Nova Praia")
                .param("descricao", "Descrição da nova praia")
                .param("localizacao", "RJ")
                .param("nivelExperiencia", "Avançado"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Nova Praia"));
    }

    @Test
    public void testGetBeachPosts() throws Exception {
        mockMvc.perform(get("/api/beaches/" + testBeachId + "/posts")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetAllBeachPosts() throws Exception {
        mockMvc.perform(get("/api/beaches/" + testBeachId + "/all-posts")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
