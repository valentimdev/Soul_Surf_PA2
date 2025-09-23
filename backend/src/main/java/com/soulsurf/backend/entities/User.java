package com.soulsurf.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    // Novo campo username
    @Column(unique = true)
    private String username;

    // Foto de capa do perfil
    private String fotoCapa;

    private String fotoPerfil;

    // Relacionamento - seguidores
    @ManyToMany
    @JoinTable(
            name = "user_seguidores",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "seguidor_id")
    )
    private java.util.List<User> seguidores = new java.util.ArrayList<>();

    // Relacionamento - seguindo
    @ManyToMany
    @JoinTable(
            name = "user_seguindo",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "seguindo_id")
    )
    private java.util.List<User> seguindo = new java.util.ArrayList<>();

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
