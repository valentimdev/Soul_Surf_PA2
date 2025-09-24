package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.dto.SignupRequest;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;

    public UserService(UserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder, PostService postService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
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
