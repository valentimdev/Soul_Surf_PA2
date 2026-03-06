package com.soulsurf.backend.modules.beach.repository;

import com.soulsurf.backend.modules.beach.entity.Beach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeachRepository extends JpaRepository<Beach, Long> {
}

