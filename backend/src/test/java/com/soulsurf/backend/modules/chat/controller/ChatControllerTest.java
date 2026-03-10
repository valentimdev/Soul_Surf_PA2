package com.soulsurf.backend.modules.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulsurf.backend.BaseIntegrationTest;
import com.soulsurf.backend.modules.chat.dto.CreateDMRequest;
import com.soulsurf.backend.modules.chat.dto.SendMessageRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ChatControllerTest extends BaseIntegrationTest {

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        private String jwtToken;
        private Long user2Id;
        private String conversationId;

        @BeforeEach
        public void setUp() throws Exception {
                userRepository.deleteAll();

                // User 1 (Current User)
                SignupRequest signup1 = new SignupRequest();
                signup1.setEmail("chatter1@example.com");
                signup1.setPassword("password123");
                signup1.setUsername("chatter1");

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signup1)))
                                .andExpect(status().isCreated());

                // User 2
                SignupRequest signup2 = new SignupRequest();
                signup2.setEmail("chatter2@example.com");
                signup2.setPassword("password123");
                signup2.setUsername("chatter2");

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signup2)))
                                .andExpect(status().isCreated());

                // Login User 1
                LoginRequest login = new LoginRequest();
                login.setEmail("chatter1@example.com");
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

                User user2 = userRepository.findByEmail("chatter2@example.com").orElseThrow();
                this.user2Id = user2.getId();

                // Create a DM conversation for use in message tests
                CreateDMRequest dmRequest = new CreateDMRequest();
                dmRequest.setOtherUserId(String.valueOf(user2Id));

                MvcResult dmResult = mockMvc.perform(post("/api/chat/dm")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dmRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                String dmResponse = dmResult.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                Map<String, Object> dmMap = objectMapper.readValue(dmResponse, Map.class);
                this.conversationId = (String) dmMap.get("conversationId");
        }

        @Test
        public void testCreateDirectMessage() throws Exception {
                // Verify conversation already created in setUp
                mockMvc.perform(get("/api/chat/conversations")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        public void testGetUserChats() throws Exception {
                mockMvc.perform(get("/api/chat/conversations")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk());
        }

        @Test
        public void testListMessages() throws Exception {
                mockMvc.perform(get("/api/chat/conversations/" + conversationId + "/messages")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        public void testSendMessage() throws Exception {
                SendMessageRequest msgRequest = new SendMessageRequest();
                msgRequest.setContent("Hello from test!");

                mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(msgRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").value("Hello from test!"));
        }
}
