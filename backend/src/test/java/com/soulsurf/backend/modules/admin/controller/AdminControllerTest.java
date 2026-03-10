package com.soulsurf.backend.modules.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
import com.soulsurf.backend.modules.comment.entity.Comment;
import com.soulsurf.backend.modules.comment.repository.CommentRepository;
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

public class AdminControllerTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BeachRepository beachRepository;

    private String jwtToken;
    private Long adminUserId;
    private Long regularUserId;
    private Long testPostId;
    private Long testCommentId;

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();

        // Create an admin user
        SignupRequest signup = new SignupRequest();
        signup.setEmail("admin@example.com");
        signup.setPassword("password123");
        signup.setUsername("adminuser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        // Make the user an admin in DB
        User admin = userRepository.findByEmail("admin@example.com").orElseThrow();
        admin.setAdmin(true);
        userRepository.save(admin);
        this.adminUserId = admin.getId();

        // Create a regular user
        SignupRequest signup2 = new SignupRequest();
        signup2.setEmail("regular@example.com");
        signup2.setPassword("password123");
        signup2.setUsername("regularuser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup2)))
                .andExpect(status().isCreated());

        User regularUser = userRepository.findByEmail("regular@example.com").orElseThrow();
        this.regularUserId = regularUser.getId();

        // Create a post for testing
        Post post = new Post();
        post.setDescricao("Test Post for Admin");
        post.setUsuario(regularUser);
        post = postRepository.save(post);
        this.testPostId = post.getId();

        // Create a comment for testing
        Comment comment = new Comment();
        comment.setTexto("Test Comment");
        comment.setPost(post);
        comment.setUsuario(regularUser);
        comment = commentRepository.save(comment);
        this.testCommentId = comment.getId();

        // Login Admin
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@example.com");
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
    public void testGetGlobalMetricsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/metrics")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditLogsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/audits")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + regularUserId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeletePost() throws Exception {
        mockMvc.perform(delete("/api/admin/posts/" + testPostId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/admin/comments/" + testCommentId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testPromoteUser() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + regularUserId + "/promote")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testDemoteUser() throws Exception {
        // First promote, then demote
        User user = userRepository.findById(regularUserId).orElseThrow();
        user.setAdmin(true);
        userRepository.save(user);

        mockMvc.perform(post("/api/admin/users/" + regularUserId + "/demote")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testBanUser() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + regularUserId + "/ban")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnbanUser() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + regularUserId + "/unban")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMetricsByPeriod() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/period")
                .header("Authorization", "Bearer " + jwtToken)
                .param("start", "2020-01-01T00:00:00")
                .param("end", "2030-12-31T23:59:59"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetTopAuthors() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-authors")
                .header("Authorization", "Bearer " + jwtToken)
                .param("start", "2020-01-01T00:00:00")
                .param("end", "2030-12-31T23:59:59")
                .param("limit", "5"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetPostsByBeach() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/by-beach")
                .header("Authorization", "Bearer " + jwtToken)
                .param("start", "2020-01-01T00:00:00")
                .param("end", "2030-12-31T23:59:59"))
                .andExpect(status().isOk());
    }
}
