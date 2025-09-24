package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.BeachDTO;
import com.soulsurf.backend.services.BeachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/beaches")
public class BeachController {

    @Autowired
    private BeachService beachService;

   
    @PostMapping
    public ResponseEntity<BeachDTO> create(@RequestBody BeachDTO dto) {
        BeachDTO created = beachService.create(dto);
        return ResponseEntity.ok(created);
    }

    
    @GetMapping
    public ResponseEntity<List<BeachDTO>> findAll() {
        return ResponseEntity.ok(beachService.findAll());
    }

   
    @GetMapping("/{id}")
    public ResponseEntity<BeachDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(beachService.findById(id));
    }

  
    @PutMapping("/{id}")
    public ResponseEntity<BeachDTO> update(@PathVariable Long id, @RequestBody BeachDTO dto) {
        return ResponseEntity.ok(beachService.update(id, dto));
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        beachService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
