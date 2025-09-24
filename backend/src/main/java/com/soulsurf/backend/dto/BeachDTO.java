package com.soulsurf.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BeachDTO {

    private Long id;

    @NotBlank(message = "O nome da praia é obrigatório")
    @Size(max = 100, message = "O nome da praia deve ter no máximo 100 caracteres")
    private String name;

    @NotBlank(message = "O estado é obrigatório")
    @Size(max = 50, message = "O estado deve ter no máximo 50 caracteres")
    private String state;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres")
    private String city;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String description;

    private Double latitude;
    private Double longitude;
}
