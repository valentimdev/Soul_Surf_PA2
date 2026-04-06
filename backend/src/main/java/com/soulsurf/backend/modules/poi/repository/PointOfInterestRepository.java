package com.soulsurf.backend.modules.poi.repository;

import com.soulsurf.backend.modules.poi.entity.PointOfInterest;
import com.soulsurf.backend.modules.poi.entity.PoiCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {
    @Override
    @EntityGraph(attributePaths = "beach")
    List<PointOfInterest> findAll();

    @EntityGraph(attributePaths = "beach")
    List<PointOfInterest> findByBeachId(Long beachId);

    @EntityGraph(attributePaths = "beach")
    List<PointOfInterest> findByCategoria(PoiCategory categoria);
}
