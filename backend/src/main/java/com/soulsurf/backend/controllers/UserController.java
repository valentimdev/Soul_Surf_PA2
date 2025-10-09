package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.security.service.UserDetailsImpl;
import com.soulsurf.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@Tag(name = "3. Usuários", description = "Endpoints para gerenciamento de perfis e interações de usuários.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Busca o perfil de um usuário", description = "Retorna os detalhes do perfil de um usuário pelo seu ID.")
    @ApiResponse(responseCode = "200", description = "Perfil do usuário encontrado")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserProfile(@Parameter(description = "ID do usuário") @PathVariable Long id) {
        return userService.getUserProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Seguir um usuário", description = "Permite que o usuário autenticado siga outro usuário. Requer autenticação JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Usuário seguido com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Usuário a ser seguido não encontrado")
    @PostMapping("/{id}/follow")
    public ResponseEntity<?> followUser(@Parameter(description = "ID do usuário a ser seguido") @PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Usuário não autenticado"));
        }
        userService.followUser(userDetails.getUsername(), id);
        return ResponseEntity.ok(new MessageResponse("Você agora está seguindo o usuário com ID " + id));
    }

    @Operation(summary = "Deixar de seguir um usuário", description = "Permite que o usuário autenticado deixe de seguir outro usuário. Requer autenticação JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Deixou de seguir o usuário com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Usuário a ser deixado de seguir não encontrado")
    @DeleteMapping("/{id}/follow")
    public ResponseEntity<?> unfollowUser(@Parameter(description = "ID do usuário a ser deixado de seguir") @PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        userService.unfollowUser(userDetails.getUsername(), id);
        return ResponseEntity.ok(new MessageResponse("Você deixou de seguir o usuário com ID " + id));
    }

    @Operation(summary = "Busca o perfil do usuário autenticado", description = "Retorna os detalhes do perfil do usuário que está logado. Requer autenticação JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Perfil do usuário encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado ou erro na conversão")
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getId();

        return userService.getUserProfile(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @Operation(summary = "Atualiza o perfil com upload de imagens", description = "Permite que o usuário autenticado atualize suas informações de perfil com fotos.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Usuário a ser atualizado não encontrado")
    @PutMapping(value = "/me/upload", consumes = "multipart/form-data")
    public ResponseEntity<UserDTO> updateUserProfileWithFiles(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Nome de usuário") @RequestParam(value = "username", required = false) String username,
            @Parameter(description = "Biografia") @RequestParam(value = "bio", required = false) String bio,
            @Parameter(description = "Arquivo de foto de perfil") @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
            @Parameter(description = "Arquivo de foto de capa") @RequestParam(value = "fotoCapa", required = false) MultipartFile fotoCapa) {

        Long userId = userDetails.getId();
        UserDTO updatedUserDTO = userService.updateUserProfileWithFiles(userId, username, bio, fotoPerfil, fotoCapa);
        return ResponseEntity.ok(updatedUserDTO);
    }

    @Operation(summary = "Lista os usuários que um usuário está seguindo", description = "Retorna a lista de usuários que o usuário com o ID especificado está seguindo.")
    @ApiResponse(responseCode = "200", description = "Lista de usuários seguidos encontrada")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserDTO>> getUserFollowing(@Parameter(description = "ID do usuário") @PathVariable Long id) {
        try {
            List<UserDTO> following = userService.getUserFollowing(id);
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Lista os seguidores de um usuário", description = "Retorna a lista de seguidores do usuário com o ID especificado.")
    @ApiResponse(responseCode = "200", description = "Lista de seguidores encontrada")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserDTO>> getUserFollowers(@Parameter(description = "ID do usuário") @PathVariable Long id) {
        try {
            List<UserDTO> followers = userService.getUserFollowers(id);
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
