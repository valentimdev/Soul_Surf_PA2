package com.soulsurf.backend.modules.beach.repository;

import com.soulsurf.backend.modules.beach.entity.BeachMessage;
import com.soulsurf.backend.modules.beach.entity.Beach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeachMessageRepository extends JpaRepository<BeachMessage, Long> {
    List<BeachMessage> findByBeachOrderByDataDesc(Beach beach);
}

