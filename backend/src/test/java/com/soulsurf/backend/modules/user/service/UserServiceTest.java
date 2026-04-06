package com.soulsurf.backend.modules.user.service;

import com.soulsurf.backend.modules.post.service.PostService;
import com.soulsurf.backend.modules.user.dto.UserDTO;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.mapper.UserMapper;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PostService postService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setId(1L);
    }

    @Test
    void testGetUserProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserDTO mockDto = new UserDTO();
        mockDto.setUsername("testuser");
        mockDto.setEmail("test@example.com");
        when(userMapper.toDto(testUser)).thenReturn(mockDto);
        when(postService.getPostsByUserEmail(eq("test@example.com"), isNull(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        Optional<UserDTO> foundUserOpt = userService.getUserProfile(1L);

        assertTrue(foundUserOpt.isPresent());
        UserDTO foundUser = foundUserOpt.get();
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserProfileNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<UserDTO> foundUserOpt = userService.getUserProfile(2L);
        assertFalse(foundUserOpt.isPresent());
        verify(userRepository, times(1)).findById(2L);
    }

    /*
     * @Test
     * void testUpdateUser() {
     * // This test will be skipped for now because updateUser requires
     * MultipartFiles
     * }
     */
}
