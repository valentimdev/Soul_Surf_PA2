package com.soulsurf.backend.modules.poi.service;

import com.soulsurf.backend.modules.poi.dto.PointOfInterestDTO;
import com.soulsurf.backend.modules.poi.entity.PointOfInterest;
import com.soulsurf.backend.modules.poi.entity.PoiCategory;
import com.soulsurf.backend.modules.poi.mapper.PoiMapper;
import com.soulsurf.backend.modules.poi.repository.PointOfInterestRepository;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointOfInterestService {

    private final PointOfInterestRepository poiRepository;
    private final BeachRepository beachRepository;
    private final PoiMapper poiMapper;

    public PointOfInterestService(PointOfInterestRepository poiRepository,
            BeachRepository beachRepository,
            PoiMapper poiMapper) {
        this.poiRepository = poiRepository;
        this.beachRepository = beachRepository;
        this.poiMapper = poiMapper;
    }

    @Transactional(readOnly = true)
    public List<PointOfInterestDTO> getAllPois() {
        return poiRepository.findAll().stream()
                .map(poiMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PointOfInterestDTO> getPoisByBeach(Long beachId) {
        return poiRepository.findByBeachId(beachId).stream()
                .map(poiMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PointOfInterestDTO> getPoisByCategory(PoiCategory categoria) {
        return poiRepository.findByCategoria(categoria).stream()
                .map(poiMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PointOfInterestDTO createPoi(PointOfInterestDTO dto) {
        PointOfInterest poi = new PointOfInterest();
        poi.setNome(dto.getNome());
        poi.setDescricao(dto.getDescricao());
        poi.setCategoria(dto.getCategoria());
        poi.setLatitude(dto.getLatitude());
        poi.setLongitude(dto.getLongitude());
        poi.setTelefone(dto.getTelefone());
        poi.setCaminhoFoto(dto.getCaminhoFoto());

        if (dto.getBeach() != null && dto.getBeach().getId() != null) {
            beachRepository.findById(dto.getBeach().getId())
                    .ifPresent(poi::setBeach);
        }

        return poiMapper.toDto(poiRepository.save(poi));
    }
}
