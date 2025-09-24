package com.soulsurf.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "beaches")
@Getter
@Setter
@NoArgsConstructor
public class Beach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da praia é obrigatório")
    @Size(max = 100, message = "O nome da praia deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "O estado é obrigatório")
    @Size(max = 50, message = "O estado deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String state;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres")
    @Column(nullable = false, length = 80)
    private String city;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String description;

    private Double latitude;
    private Double longitude;
}
