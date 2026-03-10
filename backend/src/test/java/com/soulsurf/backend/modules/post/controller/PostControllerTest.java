package com.soulsurf.backend.modules.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class PostControllerTest extends BaseIntegrationTest {

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PostRepository postRepository;

        private String jwtToken;
        private Long testPostId;
        private String userEmail = "poster@example.com";

        @BeforeEach
        public void setUp() throws Exception {
                userRepository.deleteAll();

                SignupRequest signup = new SignupRequest();
                signup.setEmail(userEmail);
                signup.setPassword("password123");
                signup.setUsername("poster");

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signup)))
                                .andExpect(status().isCreated());

                LoginRequest login = new LoginRequest();
                login.setEmail(userEmail);
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

                // Create a post via API
                MvcResult postResult = mockMvc.perform(multipart("/api/posts")
                                .header("Authorization", "Bearer " + jwtToken)
                                .param("publico", "true")
                                .param("descricao", "Amazing surf session today!"))
                                .andExpect(status().isCreated())
                                .andReturn();

                String postResponse = postResult.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                Map<String, Object> postMap = objectMapper.readValue(postResponse, Map.class);
                this.testPostId = Long.valueOf(postMap.get("id").toString());
        }

        @Test
        public void testCreateAndGetPost() throws Exception {
                // Create Post
                mockMvc.perform(multipart("/api/posts")
                                .header("Authorization", "Bearer " + jwtToken)
                                .param("publico", "true")
                                .param("descricao", "Another surf session!"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.descricao").value("Another surf session!"));

                // Get Feed
                mockMvc.perform(get("/api/posts/home")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk());
        }

        @Test
        public void testGetFollowingPosts() throws Exception {
                mockMvc.perform(get("/api/posts/following")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk());
        }

        @Test
        public void testGetPostById() throws Exception {
                mockMvc.perform(get("/api/posts/" + testPostId)
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.descricao").value("Amazing surf session today!"));
        }

        @Test
        public void testGetPostsByUser() throws Exception {
                mockMvc.perform(get("/api/posts/user")
                                .header("Authorization", "Bearer " + jwtToken)
                                .param("email", userEmail))
                                .andExpect(status().isOk());
        }

        @Test
        public void testUpdatePost() throws Exception {
                mockMvc.perform(put("/api/posts/" + testPostId)
                                .header("Authorization", "Bearer " + jwtToken)
                                .param("descricao", "Updated description"))
                                .andExpect(status().isOk());
        }

        @Test
        public void testDeletePost() throws Exception {
                mockMvc.perform(delete("/api/posts/" + testPostId)
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk());
        }
}
