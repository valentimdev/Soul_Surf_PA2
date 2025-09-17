package com.soulsurf.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


    private String fotoPerfil;

    private String fotoCapa;
    
    @NotBlank
    @Column(unique = true)
    private String username;

    @ManyToMany
    @JoinTable(
        name = "user_seguindo",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "seguindo_id")
    )
    private Set<User> seguindo = new HashSet<>();

    @ManyToMany(mappedBy = "seguindo")
    private Set<User> seguidores = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;   
    }

}