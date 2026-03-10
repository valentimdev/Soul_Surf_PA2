package com.soulsurf.backend.modules.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.user.controller.LoginRequest;
import com.soulsurf.backend.modules.user.controller.SignupRequest;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.modules.comment.entity.Comment;
import com.soulsurf.backend.modules.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class CommentControllerTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private String jwtToken;
    private Long testPostId;
    private Long testCommentId;

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();

        SignupRequest signup = new SignupRequest();
        signup.setEmail("commenter@example.com");
        signup.setPassword("password123");
        signup.setUsername("commenter");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setEmail("commenter@example.com");
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
        User user = userRepository.findByEmail("commenter@example.com").orElseThrow();
        Post post = new Post();
        post.setDescricao("Test Post");
        post.setUsuario(user);
        post = postRepository.save(post);
        this.testPostId = post.getId();

        // Create a comment for update/delete tests
        Comment comment = new Comment();
        comment.setTexto("Original Comment");
        comment.setPost(post);
        comment.setUsuario(user);
        comment = commentRepository.save(comment);
        this.testCommentId = comment.getId();
    }

    @Test
    public void testGetPostComments() throws Exception {
        mockMvc.perform(get("/api/posts/" + testPostId + "/comments/")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddComment() throws Exception {
        mockMvc.perform(post("/api/posts/" + testPostId + "/comments/")
                .header("Authorization", "Bearer " + jwtToken)
                .param("texto", "Great post!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto").value("Great post!"));
    }

    @Test
    public void testUpdateComment() throws Exception {
        mockMvc.perform(put("/api/posts/" + testPostId + "/comments/" + testCommentId)
                .header("Authorization", "Bearer " + jwtToken)
                .param("texto", "Updated comment text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto").value("Updated comment text"));
    }

    @Test
    public void testDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/posts/" + testPostId + "/comments/" + testCommentId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }
}
