package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.SignupRequest;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.dto.UserUpdateRequestDTO;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PostService postService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postService = postService;
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void registerUser(SignupRequest signupRequest) {
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setUsername(signupRequest.getUsername());
        userRepository.save(user);
    }

    public Optional<UserDTO> getUserProfile(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDto);
    }

    @Transactional
    public void followUser(String followerEmail, Long followedId) {
        User follower = userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário seguidor não encontrado."));

        User userToFollow = userRepository.findById(followedId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário a ser seguido não encontrado."));

        if (follower.getId().equals(userToFollow.getId())) {
            throw new IllegalArgumentException("Você não pode seguir a si mesmo.");
        }
        follower.getSeguindo().add(userToFollow);
        userRepository.save(follower);
    }

    @Transactional
    public void unfollowUser(String followerEmail, Long followedId) {
        User follower = userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário seguidor não encontrado."));

        User userToUnfollow = userRepository.findById(followedId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário a ser deixado de seguir não encontrado."));
        follower.getSeguindo().remove(userToUnfollow);
        userRepository.save(follower);
    }

    @Transactional
    public Optional<UserDTO> getUserProfileByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
            };

    @Transactional
    public UserDTO updateUserProfile(Long userId, UserUpdateRequestDTO updateRequest) {
    User userToUpdate = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o id: " + userId));

    if (updateRequest.getUsername() != null) {
        userToUpdate.setUsername(updateRequest.getUsername());
    }

    if (updateRequest.getFotoPerfil() != null) {
        userToUpdate.setFotoPerfil(updateRequest.getFotoPerfil());
    }
    if (updateRequest.getFotoCapa() != null) {
        userToUpdate.setFotoCapa(updateRequest.getFotoCapa());
    }

    User updatedUser = userRepository.save(userToUpdate);
    return convertToDto(updatedUser);
}
    private UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFotoPerfil(user.getFotoPerfil());
        userDTO.setFotoCapa(user.getFotoCapa());
        // Futuramente quando os posts estiverem prontos descomentar essa e trazer os posts para o perfil do usuario
        // userDTO.setPosts(user.getPosts().stream().map(this::convertToDto).collect(Collectors.toList()));
        // Para evitar recursão infinita, podemos usar um DTO mais simples ou apenas contar
        if (user.getSeguidores() != null) {
            userDTO.setSeguidoresCount(user.getSeguidores().size());
        }
        if (user.getSeguindo() != null) {
            userDTO.setSeguindoCount(user.getSeguindo().size());
        }

        userDTO.setPosts(postService.getPostsByUserEmail(user.getEmail()));

        return userDTO;
    }
}
