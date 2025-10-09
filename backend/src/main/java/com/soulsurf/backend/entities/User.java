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
    @Column(unique = true, nullable = false)
    private String username;
    // Foto de capa do perfil
    private String fotoCapa;

    private String fotoPerfil;
    private String bio; 

    // Relacionamento - seguidores (quem me segue)
    @ManyToMany(mappedBy = "seguindo", fetch = FetchType.LAZY)
    private java.util.List<User> seguidores = new java.util.ArrayList<>();

    // Relacionamento - seguindo (quem eu sigo)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_seguindo",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "seguindo_id")
    )
    private java.util.List<User> seguindo = new java.util.ArrayList<>();

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean admin = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean banned = false;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
