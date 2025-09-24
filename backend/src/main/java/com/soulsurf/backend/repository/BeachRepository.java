package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Beach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeachRepository extends JpaRepository<Beach, Long> {

    
    List<Beach> findByStateIgnoreCase(String state);

    
    List<Beach> findByCityIgnoreCase(String city);

    
    List<Beach> findByStateIgnoreCaseAndCityIgnoreCase(String state, String city);

    
    List<Beach> findByNameContainingIgnoreCase(String name);

    
    List<Beach> findByDescriptionContainingIgnoreCase(String keyword);
}
