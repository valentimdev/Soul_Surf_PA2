package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Beach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeachRepository extends JpaRepository<Beach, Long> {
}