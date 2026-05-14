package com.soulsurf.backend.modules.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.user.controller.LoginRequest;
import com.soulsurf.backend.modules.user.controller.SignupRequest;
import com.soulsurf.backend.modules.notification.entity.NotificationType;
import com.soulsurf.backend.modules.notification.repository.NotificationRepository;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.user.entity.User;
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

public class LikeControllerTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private String jwtToken;
    private Long testPostId;
    private User postOwner;

    @BeforeEach
    public void setUp() throws Exception {
        notificationRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        SignupRequest ownerSignup = new SignupRequest();
        ownerSignup.setEmail("owner@example.com");
        ownerSignup.setPassword("password123");
        ownerSignup.setUsername("owneruser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerSignup)))
                .andExpect(status().isCreated());

        // Create a different user and login to get JWT
        SignupRequest signup = new SignupRequest();
        signup.setEmail("liker@example.com");
        signup.setPassword("password123");
        signup.setUsername("likerman");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setEmail("liker@example.com");
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

        // Create a post
        postOwner = userRepository.findByEmail("owner@example.com").orElseThrow();
        Post post = new Post();
        post.setDescricao("Post to like");
        post.setUsuario(postOwner);
        post = postRepository.save(post);
        this.testPostId = post.getId();
    }

    @Test
    public void testLikePost() throws Exception {
        mockMvc.perform(post("/api/posts/" + testPostId + "/likes")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testLikePostCreatesNotificationForPostOwner() throws Exception {
        mockMvc.perform(post("/api/posts/" + testPostId + "/likes")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));

        var notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(postOwner);

        org.assertj.core.api.Assertions.assertThat(notifications)
                .hasSize(1)
                .first()
                .satisfies(notification -> {
                    org.assertj.core.api.Assertions.assertThat(notification.getType()).isEqualTo(NotificationType.LIKE);
                    org.assertj.core.api.Assertions.assertThat(notification.getSender().getEmail()).isEqualTo("liker@example.com");
                });
    }

    @Test
    public void testGetPostLikes() throws Exception {
        mockMvc.perform(get("/api/posts/" + testPostId + "/likes/count")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLikeStatus() throws Exception {
        mockMvc.perform(get("/api/posts/" + testPostId + "/likes/status")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").exists());
    }
}
