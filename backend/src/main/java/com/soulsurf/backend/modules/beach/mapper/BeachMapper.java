package com.soulsurf.backend.modules.beach.mapper;

import com.soulsurf.backend.modules.beach.dto.BeachDTO;
import com.soulsurf.backend.modules.beach.entity.Beach;
import org.springframework.stereotype.Component;

@Component
public class BeachMapper {

    public BeachDTO toDto(Beach beach) {
        BeachDTO dto = new BeachDTO();
        dto.setId(beach.getId());
        dto.setNome(beach.getNome());
        dto.setDescricao(beach.getDescricao());
        dto.setLocalizacao(beach.getLocalizacao());
        dto.setCaminhoFoto(beach.getCaminhoFoto());
        dto.setNivelExperiencia(beach.getNivelExperiencia());
        dto.setLatitude(beach.getLatitude());
        dto.setLongitude(beach.getLongitude());
        return dto;
    }
}
