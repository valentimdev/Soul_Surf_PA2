package com.soulsurf.backend.modules.chat.service;

import com.soulsurf.backend.modules.chat.entity.Conversation;
import com.soulsurf.backend.modules.chat.repository.ConversationParticipantRepository;
import com.soulsurf.backend.modules.chat.repository.ConversationRepository;
import com.soulsurf.backend.modules.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private ConversationRepository convRepo;

    @Mock
    private ConversationParticipantRepository partRepo;

    @Mock
    private MessageRepository msgRepo;

    @InjectMocks
    private ChatService chatService;

    private String userA = "user1@example.com";
    private String userB = "user2@example.com";
    private Conversation existingConv;

    @BeforeEach
    void setUp() {
        existingConv = new Conversation();
        existingConv.setId("conv-123");
        existingConv.setGroup(false);
    }

    @Test
    void testEnsureDMFoundExisting() {
        when(partRepo.findDMBetween(userA, userB)).thenReturn("conv-123");
        when(convRepo.findById("conv-123")).thenReturn(Optional.of(existingConv));

        Conversation result = chatService.ensureDM(userA, userB);

        assertNotNull(result);
        assertEquals("conv-123", result.getId());
        verify(convRepo, never()).save(any(Conversation.class));
    }

    @Test
    void testEnsureDMCreatesNew() {
        when(partRepo.findDMBetween(userA, userB)).thenReturn(null);
        
        Conversation newConv = new Conversation();
        newConv.setId("new-conv");
        when(convRepo.save(any(Conversation.class))).thenReturn(newConv);

        Conversation result = chatService.ensureDM(userA, userB);

        assertNotNull(result);
        assertEquals("new-conv", result.getId());
        verify(convRepo, times(1)).save(any(Conversation.class));
        verify(partRepo, times(1)).saveAll(any());
    }

    @Test
    void testEnsureDMSameUserThrowsException() {
        assertThrows(RuntimeException.class, () -> chatService.ensureDM(userA, userA));
    }
}
