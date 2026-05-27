package com.soulsurf.backend.modules.poi.service;

import com.soulsurf.backend.modules.poi.dto.PointOfInterestDTO;
import com.soulsurf.backend.modules.poi.entity.PointOfInterest;
import com.soulsurf.backend.modules.poi.entity.PoiCategory;
import com.soulsurf.backend.modules.poi.mapper.PoiMapper;
import com.soulsurf.backend.modules.poi.repository.PointOfInterestRepository;
import com.soulsurf.backend.core.storage.OracleStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PointOfInterestService {

    private final PointOfInterestRepository poiRepository;
    private final PoiMapper poiMapper;
    private final Optional<OracleStorageService> blobStorageService;

    public PointOfInterestService(PointOfInterestRepository poiRepository,
            PoiMapper poiMapper,
            Optional<OracleStorageService> blobStorageService) {
        this.poiRepository = poiRepository;
        this.poiMapper = poiMapper;
        this.blobStorageService = blobStorageService;
    }

    @Transactional(readOnly = true)
    public List<PointOfInterestDTO> getAllPois() {
        return poiRepository.findAll().stream()
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
    public PointOfInterestDTO createPoi(String nome, String descricao, PoiCategory categoria,
            Double latitude, Double longitude, String telefone, MultipartFile foto) {
        PointOfInterest poi = new PointOfInterest();
        poi.setNome(nome);
        poi.setDescricao(descricao);
        poi.setCategoria(categoria);
        poi.setLatitude(latitude);
        poi.setLongitude(longitude);
        poi.setTelefone(telefone);

        if (blobStorageService.isPresent() && foto != null && !foto.isEmpty()) {
            try {
                String urlDaFoto = blobStorageService.get().uploadFile(foto);
                poi.setCaminhoFoto(urlDaFoto);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao fazer upload da foto do ponto de interesse: " + e.getMessage(), e);
            }
        }

        return poiMapper.toDto(poiRepository.save(poi));
    }
}
