package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Beach;
import com.soulsurf.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeachRepository extends JpaRepository<Beach, Long> {
}