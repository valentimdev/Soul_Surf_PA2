package com.soulsurf.backend.modules.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.post.repository.PostRepository;
import com.soulsurf.backend.modules.comment.entity.Comment;
import com.soulsurf.backend.modules.comment.repository.CommentRepository;
import com.soulsurf.backend.modules.user.controller.LoginRequest;
import com.soulsurf.backend.modules.user.controller.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.modules.notification.repository.NotificationRepository;

public class NotificationControllerTest extends BaseIntegrationTest {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private NotificationRepository notificationRepository;

        @Autowired
        private PostRepository postRepository;

        @Autowired
        private CommentRepository commentRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private String jwtToken;
        private String otherJwtToken;
        private Long testPostId;
        private Long testCommentId;
        private Long parentCommentId;

        @BeforeEach
        public void setUp() throws Exception {
                userRepository.deleteAll();

                // Main user (will be the sender)
                SignupRequest signup = new SignupRequest();
                signup.setEmail("sender@example.com");
                signup.setPassword("password123");
                signup.setUsername("sender");

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signup)))
                                .andExpect(status().isCreated());

                // Other user (will be the post owner / recipient)
                SignupRequest signup2 = new SignupRequest();
                signup2.setEmail("recipient@example.com");
                signup2.setPassword("password123");
                signup2.setUsername("recipient");

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signup2)))
                                .andExpect(status().isCreated());

                // Login sender
                LoginRequest login = new LoginRequest();
                login.setEmail("sender@example.com");
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

                // Login recipient
                LoginRequest login2 = new LoginRequest();
                login2.setEmail("recipient@example.com");
                login2.setPassword("password123");

                MvcResult result2 = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login2)))
                                .andExpect(status().isOk())
                                .andReturn();

                String response2 = result2.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                Map<String, Object> map2 = objectMapper.readValue(response2, Map.class);
                this.otherJwtToken = (String) map2.get("token");

                // Create test data owned by the recipient (so sender != post owner)
                User recipientUser = userRepository.findByEmail("recipient@example.com").orElseThrow();
                User senderUser = userRepository.findByEmail("sender@example.com").orElseThrow();

                Post post = new Post();
                post.setDescricao("Test post for notifications");
                post.setUsuario(recipientUser);
                post = postRepository.save(post);
                this.testPostId = post.getId();

                Comment parentComment = new Comment();
                parentComment.setTexto("Parent comment");
                parentComment.setPost(post);
                parentComment.setUsuario(recipientUser);
                parentComment = commentRepository.save(parentComment);
                this.parentCommentId = parentComment.getId();

                Comment childComment = new Comment();
                childComment.setTexto("Child comment by sender");
                childComment.setPost(post);
                childComment.setUsuario(senderUser);
                childComment.setParentComment(parentComment);
                childComment = commentRepository.save(childComment);
                this.testCommentId = childComment.getId();
        }

        @Test
        public void testGetUserNotifications() throws Exception {
                mockMvc.perform(get("/api/notifications/")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        public void testGetUnreadNotifications() throws Exception {
                mockMvc.perform(get("/api/notifications/count")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk());
        }

        @Test
        public void testMarkAsRead() throws Exception {
                User user = userRepository.findByUsername("sender").orElseThrow();
                com.soulsurf.backend.modules.notification.entity.Notification notif = new com.soulsurf.backend.modules.notification.entity.Notification();
                notif.setRecipient(user);
                notif.setSender(user);
                notif.setType(com.soulsurf.backend.modules.notification.entity.NotificationType.LIKE);
                notif.setRead(false);
                notif = notificationRepository.save(notif);

                mockMvc.perform(put("/api/notifications/" + notif.getId() + "/read")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk());
        }

        @Test
        public void testCreateMentionNotification() throws Exception {
                mockMvc.perform(post("/api/notifications/mention")
                                .header("Authorization", "Bearer " + jwtToken)
                                .param("recipientUsername", "recipient")
                                .param("postId", testPostId.toString()))
                                .andExpect(status().isOk());
        }

        @Test
        public void testCreateCommentNotification() throws Exception {
                mockMvc.perform(post("/api/notifications/comment")
                                .header("Authorization", "Bearer " + jwtToken)
                                .param("postId", testPostId.toString())
                                .param("commentId", testCommentId.toString()))
                                .andExpect(status().isOk());
        }

        @Test
        public void testCreateReplyNotification() throws Exception {
                mockMvc.perform(post("/api/notifications/reply")
                                .header("Authorization", "Bearer " + jwtToken)
                                .param("postId", testPostId.toString())
                                .param("commentId", testCommentId.toString())
                                .param("parentCommentId", parentCommentId.toString()))
                                .andExpect(status().isOk());
        }
}
