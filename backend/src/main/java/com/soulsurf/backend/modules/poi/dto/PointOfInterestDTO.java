package com.soulsurf.backend.modules.poi.dto;

import com.soulsurf.backend.modules.poi.entity.PoiCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PointOfInterestDTO {
    private Long id;
    private String nome;
    private String descricao;
    private PoiCategory categoria;
    private Double latitude;
    private Double longitude;
    private String telefone;
    private String caminhoFoto;
}
