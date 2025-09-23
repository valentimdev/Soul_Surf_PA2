package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.dto.UserUpdateRequestDTO;
import com.soulsurf.backend.security.service.UserDetailsImpl;
import com.soulsurf.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> followUser(@Parameter(description = "ID do usuário a ser seguido") @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
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
    public ResponseEntity<?> unfollowUser(@Parameter(description = "ID do usuário a ser deixado de seguir") @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        userService.unfollowUser(userDetails.getUsername(), id);
        return ResponseEntity.ok(new MessageResponse("Você deixou de seguir o usuário com ID " + id));
    }

    
    @Operation(summary = "Busca o perfil do usuário autenticado", description = "Retorna os detalhes do perfil do usuário que está logado. Requer autenticação JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Perfil do usuário encontrado")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado ou erro na conversão")
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();
        
        return userService.getUserProfile(userId)
                .map(ResponseEntity::ok) 
                .orElse(ResponseEntity.notFound().build());
    }
    @Operation(summary = "Atualiza o perfil do usuário autenticado", description = "Permite que o usuário autenticado atualize suas informações de perfil (nome, bio, fotos, etc).", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Usuário a ser atualizado não encontrado")
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateUserProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody UserUpdateRequestDTO updateRequest) {
                
        Long userId = userDetails.getId();
            
        UserDTO updatedUserDTO = userService.updateUserProfile(userId, updateRequest);
            
        return ResponseEntity.ok(updatedUserDTO);
    }
}