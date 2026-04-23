package com.soulsurf.backend.modules.poi.repository;

import com.soulsurf.backend.modules.poi.entity.PointOfInterest;
import com.soulsurf.backend.modules.poi.entity.PoiCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {
    List<PointOfInterest> findByCategoria(PoiCategory categoria);
}
