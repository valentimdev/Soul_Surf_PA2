package com.soulsurf.backend.modules.poi.controller;

import com.soulsurf.backend.modules.poi.dto.PointOfInterestDTO;
import com.soulsurf.backend.modules.poi.entity.PoiCategory;
import com.soulsurf.backend.modules.poi.service.PointOfInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pois")
@Tag(name = "Points of Interest", description = "Endpoints para gerenciamento de pontos de interesse (escolas, lojas, etc)")
public class PointOfInterestController {

    private final PointOfInterestService poiService;

    public PointOfInterestController(PointOfInterestService poiService) {
        this.poiService = poiService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os pontos de interesse")
    public List<PointOfInterestDTO> getAllPois() {
        return poiService.getAllPois();
    }

    @GetMapping("/beach/{beachId}")
    @Operation(summary = "Lista pontos de interesse por praia")
    public List<PointOfInterestDTO> getPoisByBeach(@PathVariable Long beachId) {
        return poiService.getPoisByBeach(beachId);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Lista pontos de interesse por categoria")
    public List<PointOfInterestDTO> getPoisByCategory(@PathVariable PoiCategory category) {
        return poiService.getPoisByCategory(category);
    }

    @PostMapping
    @Operation(summary = "Cria um novo ponto de interesse")
    public ResponseEntity<PointOfInterestDTO> createPoi(@RequestBody PointOfInterestDTO dto) {
        return ResponseEntity.ok(poiService.createPoi(dto));
    }
}
