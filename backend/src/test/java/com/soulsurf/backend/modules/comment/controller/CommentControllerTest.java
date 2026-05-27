package com.soulsurf.backend.modules.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.notification.entity.Notification;
import com.soulsurf.backend.modules.notification.entity.NotificationType;
import com.soulsurf.backend.modules.notification.repository.NotificationRepository;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Autowired
    private NotificationRepository notificationRepository;

    private String jwtToken;
    private Long testPostId;
    private Long testCommentId;

    @BeforeEach
    public void setUp() throws Exception {
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testAddCommentCreatesNotificationForPostOwnerAfterCommit() throws Exception {
        try {
            SignupRequest ownerSignup = new SignupRequest();
            ownerSignup.setEmail("comment-owner@example.com");
            ownerSignup.setPassword("password123");
            ownerSignup.setUsername("commentowner");

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ownerSignup)))
                    .andExpect(status().isCreated());

            User postOwner = userRepository.findByEmail("comment-owner@example.com").orElseThrow();
            Post ownerPost = new Post();
            ownerPost.setDescricao("Post owned by someone else");
            ownerPost.setUsuario(postOwner);
            ownerPost = postRepository.save(ownerPost);

            mockMvc.perform(post("/api/posts/" + ownerPost.getId() + "/comments/")
                    .header("Authorization", "Bearer " + jwtToken)
                    .param("texto", "Great post from another user!"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.texto").value("Great post from another user!"));

            var notifications = waitForNotifications(postOwner);

            org.assertj.core.api.Assertions.assertThat(notifications)
                    .hasSize(1)
                    .first()
                    .satisfies(notification -> {
                        org.assertj.core.api.Assertions.assertThat(notification.getType()).isEqualTo(NotificationType.COMMENT);
                        org.assertj.core.api.Assertions.assertThat(notification.getSender().getEmail())
                                .isEqualTo("commenter@example.com");
                    });
        } finally {
            cleanDatabase();
        }
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

    private void cleanDatabase() {
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private List<Notification> waitForNotifications(User recipient) throws InterruptedException {
        for (int attempt = 0; attempt < 20; attempt++) {
            var notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
            if (!notifications.isEmpty()) {
                return notifications;
            }
            Thread.sleep(50);
        }

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }
}
