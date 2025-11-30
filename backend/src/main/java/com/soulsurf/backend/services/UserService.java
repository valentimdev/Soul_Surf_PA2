package com.soulsurf.backend.services;

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

import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.dto.SignupRequest;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.FollowRepository;
import com.soulsurf.backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;
    private final java.util.Optional<BlobStorageService> blobStorageService;
    private final FollowRepository followRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PostService postService,
            java.util.Optional<BlobStorageService> blobStorageService,
            FollowRepository followRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postService = postService;
        this.blobStorageService = blobStorageService;
        this.followRepository = followRepository;
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
        return convertToDto(savedUser);
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

        // Verifica se já está seguindo para evitar duplicatas
        if (!follower.getSeguindo().contains(userToFollow)) {
            // Com o relacionamento bidirecional, só precisamos adicionar em uma direção
            // O JPA automaticamente atualiza a lista de seguidores do userToFollow
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

        // Com o relacionamento bidirecional, só precisamos remover de uma direção
        // O JPA automaticamente atualiza a lista de seguidores do userToUnfollow
        follower.getSeguindo().remove(userToUnfollow);
        userRepository.save(follower);
    }

    @Transactional
    public Optional<UserDTO> getUserProfileByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }


    public List<UserDTO> getUserFollowing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o ID: " + userId));

        return user.getSeguindo().stream()
                .map(this::convertToDtoWithoutPosts)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUserFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o ID: " + userId));

        return user.getSeguidores().stream()
                .map(this::convertToDtoWithoutPosts)

                .collect(Collectors.toList());
    }

//     @Transactional
//     public UserDTO updateUserProfile(Long userId, UserUpdateRequestDTO updateRequest) {
//     User userToUpdate = userRepository.findById(userId)
//             .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o id: " + userId));

//     if (updateRequest.getUsername() != null) {
//         userToUpdate.setUsername(updateRequest.getUsername());
//     }

//     if (updateRequest.getFotoPerfil() != null) {
//         userToUpdate.setFotoPerfil(updateRequest.getFotoPerfil());
//     }
//     if (updateRequest.getFotoCapa() != null) {
//         userToUpdate.setFotoCapa(updateRequest.getFotoCapa());
//     }

//     User updatedUser = userRepository.save(userToUpdate);
//     return convertToDto(updatedUser);
// }

    @Transactional
    public UserDTO updateUserProfileWithFiles(Long userId, String username,String bio, MultipartFile fotoPerfil, MultipartFile fotoCapa) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o id: " + userId));

        if (username != null) {
            // Validação do formato do username: apenas letras, números, underscore e hífen
            Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
            if (!usernamePattern.matcher(username).matches()) {
                throw new IllegalArgumentException("O username deve conter apenas letras, números, underscore (_) ou hífen (-). Não são permitidos espaços ou acentos.");
            }

            // Verifica se o username já está em uso por outro usuário
            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("O username já está em uso por outro usuário.");
            }

            userToUpdate.setUsername(username);
        }
        if (bio != null) {
            userToUpdate.setBio(bio);
        }

        // Upload da foto de perfil se fornecida
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

        // Upload da foto de capa se fornecida
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
        return convertToDto(updatedUser);
    }

    /**
     * Busca sugestões de usuários para menções em comentários
     * Prioriza usuários que o usuário atual segue
     * @param searchTerm O termo de busca (após o @)
     * @param currentUserEmail Email do usuário atual
     * @param limit Número máximo de sugestões a retornar
     * @return Lista de UserDTOs simplificados para sugestões
     */
    @Transactional
    public List<UserDTO> getUserSuggestions(String searchTerm, String currentUserEmail, int limit) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Busca primeiro os usuários seguidos que correspondem à pesquisa
        List<User> followedUsers = userRepository.findFollowedUsersContainingUsername(
                currentUser.getId(), searchTerm);

        // Se não encontrou o suficiente, busca outros usuários
        if (followedUsers.size() < limit) {
            List<User> otherUsers = userRepository.findByUsernameContainingIgnoreCase(searchTerm);

            // Remove usuários que já estão incluídos
            List<User> filteredOtherUsers = otherUsers.stream()
                    .filter(user -> !followedUsers.contains(user))
                    .limit(limit - followedUsers.size())
                    .collect(Collectors.toList());

            followedUsers.addAll(filteredOtherUsers);
        }

        // Retorna apenas o necessário para mostrar as sugestões
        return followedUsers.stream()
                .limit(limit)
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setFotoPerfil(user.getFotoPerfil());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFotoPerfil(user.getFotoPerfil());
        userDTO.setFotoCapa(user.getFotoCapa());
        userDTO.setBio(user.getBio());
        userDTO.setAdmin(user.isAdmin());
        userDTO.setBanned(user.isBanned());
        // Futuramente quando os posts estiverem prontos descomentar essa e trazer os posts para o perfil do usuario
        // userDTO.setPosts(user.getPosts().stream().map(this::convertToDto).collect(Collectors.toList()));
        // Para evitar recursão infinita, podemos usar um DTO mais simples ou apenas contar
        if (user.getSeguidores() != null) {
            userDTO.setSeguidoresCount(user.getSeguidores().size());
        }
        if (user.getSeguindo() != null) {
            userDTO.setSeguindoCount(user.getSeguindo().size());
        }

        // Para o perfil, pegamos apenas os primeiros 10 posts
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "data"));
        Page<PostDTO> postsPage = postService.getPostsByUserEmail(user.getEmail(), pageRequest);
        userDTO.setPosts(postsPage.getContent());

        return userDTO;
    }
    private UserDTO convertToDtoWithoutPosts(User user) {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(user.getId());
    userDTO.setUsername(user.getUsername());
    userDTO.setEmail(user.getEmail());
    userDTO.setFotoPerfil(user.getFotoPerfil());
    userDTO.setFotoCapa(user.getFotoCapa());
    userDTO.setBio(user.getBio());
    userDTO.setAdmin(user.isAdmin());
    userDTO.setBanned(user.isBanned());


    if (user.getSeguidores() != null) {
        userDTO.setSeguidoresCount(user.getSeguidores().size());
    }
    if (user.getSeguindo() != null) {
        userDTO.setSeguindoCount(user.getSeguindo().size());
    }

    return userDTO;
    }

    public List<UserDTO> getAllUsersPaginated(int offset, int limit, Long loggedUserId) {
        List<User> users = userRepository.findAllWithPagination(offset, limit);

        return users.stream()
                .map(this::convertToDtoWithoutPosts)
                .peek(dto -> dto.setFollowing(followRepository.existsByFollowerIdAndFollowingId(loggedUserId, dto.getId())))
                .collect(Collectors.toList());
    }

    public List<UserDTO> searchUsers(String query, Long loggedUserId) {
        List<User> users = userRepository.searchUsers(query);

        return users.stream()
                .filter(u -> !u.getId().equals(loggedUserId))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(User::getId, u -> u, (a, b) -> a),
                        map -> map.values().stream()
                ))
                .map(this::convertToDtoWithoutPosts)
                .peek(dto -> dto.setFollowing(
                        followRepository.existsByFollowerIdAndFollowingId(loggedUserId, dto.getId())
                ))
                .collect(Collectors.toList());
    }

}
