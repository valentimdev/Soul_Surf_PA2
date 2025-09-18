package com.soulsurf.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.services.UserService;

import io.jsonwebtoken.io.IOException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //rota para pegar o perfil de um usuario pelo id
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    //rota do follow
    @PostMapping("/{id}/follow")
    public ResponseEntity<?> followUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        userService.followUser(userDetails.getUsername(), id);
        return ResponseEntity.ok(new MessageResponse("Você agora está seguindo o usuário com ID " + id));
    }

    //rota para dar unfollow 
    @DeleteMapping("/{id}/follow")
    public ResponseEntity<?> unfollowUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        userService.unfollowUser(userDetails.getUsername(), id);
        return ResponseEntity.ok(new MessageResponse("Você deixou de seguir o usuário com ID " + id));
    }

    //rota para ver o proprio perfil
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserProfileByEmail(userDetails.getUsername())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    //rota para trocar foto de capa e de perfil
    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(
            @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
            @RequestParam(value = "fotoCapa", required = false) MultipartFile fotoCapa,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            userService.updateProfilePictures(userDetails.getUsername(), fotoPerfil, fotoCapa);
            return ResponseEntity.ok(new MessageResponse("Perfil atualizado com sucesso!"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Erro ao atualizar o perfil."));
        }
    }

    //rota que retorna uma lista dos seguidores do usuario
    @GetMapping("/{username}/followers")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable String username) {
        List<UserDTO> followers = userService.getFollowers(username);
        return ResponseEntity.ok(followers);
    }
    //rota que retorna uma lista dos seguindo do usuario
    @GetMapping("/{username}/following")
    public ResponseEntity<List<UserDTO>> getFollowing(@PathVariable String username) {
        List<UserDTO> following = userService.getFollowing(username);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/hello")
    public String helloWorld() {
        return "teste";
    }
}