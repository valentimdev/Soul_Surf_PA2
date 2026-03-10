package com.soulsurf.backend.modules.user.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.soulsurf.backend.modules.post.dto.PostDTO;
import com.soulsurf.backend.modules.user.controller.SignupRequest;
import com.soulsurf.backend.core.storage.OracleStorageService;
import com.soulsurf.backend.modules.user.dto.UserDTO;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.user.mapper.UserMapper;
import com.soulsurf.backend.modules.user.repository.FollowRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import com.soulsurf.backend.modules.post.service.PostService;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;
    private final Optional<OracleStorageService> blobStorageService;
    private final FollowRepository followRepository;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PostService postService,
            Optional<OracleStorageService> blobStorageService,
            FollowRepository followRepository,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postService = postService;
        this.blobStorageService = blobStorageService;
        this.followRepository = followRepository;
        this.userMapper = userMapper;
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public UserDTO registerUser(SignupRequest signupRequest) {
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setUsername(signupRequest.getUsername());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public Optional<UserDTO> getUserProfile(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDtoWithPosts);
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
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

        if (!follower.getSeguindo().contains(userToFollow)) {
            follower.getSeguindo().add(userToFollow);
            userRepository.save(follower);
        }
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
                .map(this::convertToDtoWithPosts);
    }

    public List<UserDTO> getUserFollowing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o ID: " + userId));

        return user.getSeguindo().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUserFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o ID: " + userId));

        return user.getSeguidores().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUserProfileWithFiles(Long userId, String username, String bio, MultipartFile fotoPerfil,
            MultipartFile fotoCapa) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o id: " + userId));

        if (username != null) {
            Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
            if (!usernamePattern.matcher(username).matches()) {
                throw new IllegalArgumentException(
                        "O username deve conter apenas letras, números, underscore (_) ou hífen (-). Não são permitidos espaços ou acentos.");
            }

            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("O username já está em uso por outro usuário.");
            }

            userToUpdate.setUsername(username);
        }
        if (bio != null) {
            userToUpdate.setBio(bio);
        }

        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            if (blobStorageService.isPresent()) {
                try {
                    String fotoPerfilUrl = blobStorageService.get().uploadFile(fotoPerfil);
                    userToUpdate.setFotoPerfil(fotoPerfilUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao fazer upload da foto de perfil: " + e.getMessage());
                }
            } else {
                throw new RuntimeException("Serviço de armazenamento de arquivos não está disponível");
            }
        }

        if (fotoCapa != null && !fotoCapa.isEmpty()) {
            if (blobStorageService.isPresent()) {
                try {
                    String fotoCapaUrl = blobStorageService.get().uploadFile(fotoCapa);
                    userToUpdate.setFotoCapa(fotoCapaUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao fazer upload da foto de capa: " + e.getMessage());
                }
            } else {
                throw new RuntimeException("Serviço de armazenamento de arquivos não está disponível");
            }
        }

        User updatedUser = userRepository.save(userToUpdate);
        return convertToDtoWithPosts(updatedUser);
    }

    @Transactional
    public List<UserDTO> getUserSuggestions(String searchTerm, String currentUserEmail, int limit) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        List<User> followedUsers = userRepository.findFollowedUsersContainingUsername(
                currentUser.getId(), searchTerm);

        if (followedUsers.size() < limit) {
            List<User> otherUsers = userRepository.findByUsernameContainingIgnoreCase(searchTerm);

            List<User> filteredOtherUsers = otherUsers.stream()
                    .filter(user -> !followedUsers.contains(user))
                    .limit(limit - followedUsers.size())
                    .collect(Collectors.toList());

            followedUsers.addAll(filteredOtherUsers);
        }

        return followedUsers.stream()
                .limit(limit)
                .map(userMapper::toMentionDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a User to UserDTO including the first 10 posts.
     * Used for full profile views.
     */
    private UserDTO convertToDtoWithPosts(User user) {
        UserDTO dto = userMapper.toDto(user);

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "data"));
        Page<PostDTO> postsPage = postService.getPostsByUserEmail(user.getEmail(), pageRequest);
        dto.setPosts(postsPage.getContent());

        return dto;
    }

    public List<UserDTO> getAllUsersPaginated(int offset, int limit, Long loggedUserId) {
        List<User> users = userRepository.findAllWithPagination(offset, limit);

        return users.stream()
                .map(userMapper::toDto)
                .peek(dto -> dto
                        .setFollowing(followRepository.existsByFollowerIdAndFollowingId(loggedUserId, dto.getId())))
                .collect(Collectors.toList());
    }

    public List<UserDTO> searchUsers(String query, Long loggedUserId) {
        List<User> users = userRepository.searchUsers(query);

        return users.stream()
                .filter(u -> !u.getId().equals(loggedUserId))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(User::getId, u -> u, (a, b) -> a),
                        map -> map.values().stream()))
                .map(userMapper::toDto)
                .peek(dto -> dto.setFollowing(
                        followRepository.existsByFollowerIdAndFollowingId(loggedUserId, dto.getId())))
                .collect(Collectors.toList());
    }
}
