package com.soulsurf.backend.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;
    private Long testUserId;
    private Long otherUserId;

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();

        // Create main user
        SignupRequest signup = new SignupRequest();
        signup.setEmail("user@example.com");
        signup.setPassword("password123");
        signup.setUsername("testuser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        // Create another user for follow tests
        SignupRequest signup2 = new SignupRequest();
        signup2.setEmail("other@example.com");
        signup2.setPassword("password123");
        signup2.setUsername("otheruser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup2)))
                .andExpect(status().isCreated());

        // Login main user
        LoginRequest login = new LoginRequest();
        login.setEmail("user@example.com");
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

        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        this.testUserId = user.getId();

        User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();
        this.otherUserId = otherUser.getId();
    }

    @Test
    public void testGetUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testSearchUsers() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("query", "other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetMyFollowing() throws Exception {
        mockMvc.perform(get("/api/users/following")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetUserProfileByUsername() throws Exception {
        mockMvc.perform(get("/api/users/username/testuser")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testGetUserProfileById() throws Exception {
        mockMvc.perform(get("/api/users/" + testUserId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testFollowUser() throws Exception {
        mockMvc.perform(post("/api/users/" + otherUserId + "/follow")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnfollowUser() throws Exception {
        // First follow, then unfollow
        mockMvc.perform(post("/api/users/" + otherUserId + "/follow")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/" + otherUserId + "/follow")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateUserProfileWithFiles() throws Exception {
        mockMvc.perform(multipart("/api/users/me/upload")
                .header("Authorization", "Bearer " + jwtToken)
                .param("username", "updateduser")
                .param("bio", "Surfer life")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetUserFollowing() throws Exception {
        mockMvc.perform(get("/api/users/" + testUserId + "/following")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetUserFollowers() throws Exception {
        mockMvc.perform(get("/api/users/" + testUserId + "/followers")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetUserSuggestions() throws Exception {
        mockMvc.perform(get("/api/users/mention-suggestions")
                .header("Authorization", "Bearer " + jwtToken)
                .param("query", "other")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + jwtToken)
                .param("offset", "0")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
