package com.soulsurf.backend.modules.poi.mapper;

import com.soulsurf.backend.modules.poi.dto.PointOfInterestDTO;
import com.soulsurf.backend.modules.poi.entity.PointOfInterest;
import com.soulsurf.backend.modules.beach.mapper.BeachMapper;
import org.springframework.stereotype.Component;

@Component
public class PoiMapper {

    private final BeachMapper beachMapper;

    public PoiMapper(BeachMapper beachMapper) {
        this.beachMapper = beachMapper;
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

        if (poi.getBeach() != null) {
            dto.setBeach(beachMapper.toDto(poi.getBeach()));
        }

        return dto;
    }
}
