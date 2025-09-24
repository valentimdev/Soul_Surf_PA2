package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.BeachDTO;
import com.soulsurf.backend.entities.Beach;
import com.soulsurf.backend.repository.BeachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BeachService {

    @Autowired
    private BeachRepository beachRepository;

    // Converter Entity -> DTO
    private BeachDTO toDTO(Beach entity) {
        BeachDTO dto = new BeachDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setState(entity.getState());
        dto.setCity(entity.getCity());
        dto.setDescription(entity.getDescription());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        return dto;
    }

    // Converter DTO -> Entity
    private Beach toEntity(BeachDTO dto) {
        Beach entity = new Beach();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setState(dto.getState());
        entity.setCity(dto.getCity());
        entity.setDescription(dto.getDescription());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        return entity;
    }

    // Criar nova praia
    public BeachDTO create(BeachDTO dto) {
        Beach entity = toEntity(dto);
        Beach saved = beachRepository.save(entity);
        return toDTO(saved);
    }

    // Listar todas
    public List<BeachDTO> findAll() {
        return beachRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    
    public BeachDTO findById(Long id) {
        Beach entity = beachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada com ID: " + id));
        return toDTO(entity);
    }

   
    public BeachDTO update(Long id, BeachDTO dto) {
        Beach entity = beachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada com ID: " + id));

        entity.setName(dto.getName());
        entity.setState(dto.getState());
        entity.setCity(dto.getCity());
        entity.setDescription(dto.getDescription());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());

        Beach updated = beachRepository.save(entity);
        return toDTO(updated);
    }

   
    public void delete(Long id) {
        if (!beachRepository.existsById(id)) {
            throw new RuntimeException("Praia não encontrada com ID: " + id);
        }
        beachRepository.deleteById(id);
    }
}
