package com.soulsurf.backend.modules.poi.mapper;

import com.soulsurf.backend.modules.poi.dto.PointOfInterestDTO;
import com.soulsurf.backend.modules.poi.entity.PointOfInterest;
import org.springframework.stereotype.Component;

@Component
public class PoiMapper {

    public PoiMapper() {
    }

    public PointOfInterestDTO toDto(PointOfInterest poi) {
        if (poi == null)
            return null;

        PointOfInterestDTO dto = new PointOfInterestDTO();
        dto.setId(poi.getId());
        dto.setNome(poi.getNome());
        dto.setDescricao(poi.getDescricao());
        dto.setCategoria(poi.getCategoria());
        dto.setLatitude(poi.getLatitude());
        dto.setLongitude(poi.getLongitude());
        dto.setTelefone(poi.getTelefone());
        dto.setCaminhoFoto(poi.getCaminhoFoto());

        return dto;
    }
}
