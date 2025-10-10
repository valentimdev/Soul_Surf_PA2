package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.Mensagem;
import com.soulsurf.backend.entities.Beach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByBeachOrderByDataDesc(Beach beach);
}