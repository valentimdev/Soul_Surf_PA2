package com.soulsurf.backend.modules.beach.service;

import com.soulsurf.backend.modules.beach.dto.BeachDTO;
import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
import com.soulsurf.backend.modules.beach.mapper.BeachMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeachServiceTest {

    @Mock
    private BeachRepository beachRepository;

    @Mock
    private BeachMapper beachMapper;

    @InjectMocks
    private BeachService beachService;

    private Beach testBeach;

    @BeforeEach
    void setUp() {
        testBeach = new Beach();
        testBeach.setId(1L);
        testBeach.setNome("Maresias");
    }

    @Test
    void testGetAllBeaches() {
        when(beachRepository.findAll()).thenReturn(Arrays.asList(testBeach));
        
        BeachDTO mockDto = new BeachDTO();
        mockDto.setNome("Maresias");
        when(beachMapper.toDto(any(Beach.class))).thenReturn(mockDto);

        List<BeachDTO> beaches = beachService.getAllBeaches();

        assertNotNull(beaches);
        assertFalse(beaches.isEmpty());
        assertEquals(1, beaches.size());
        assertEquals("Maresias", beaches.get(0).getNome());
        verify(beachRepository, times(1)).findAll();
    }

    @Test
    void testGetBeachByIdFound() {
        when(beachRepository.findById(1L)).thenReturn(Optional.of(testBeach));
        
        BeachDTO mockDto = new BeachDTO();
        mockDto.setNome("Maresias");
        when(beachMapper.toDto(any(Beach.class))).thenReturn(mockDto);

        Optional<BeachDTO> foundBeachOpt = beachService.getBeachById(1L);

        assertTrue(foundBeachOpt.isPresent());
        verify(beachRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBeachByIdNotFound() {
        when(beachRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<BeachDTO> foundBeachOpt = beachService.getBeachById(2L);
        assertFalse(foundBeachOpt.isPresent());
        verify(beachRepository, times(1)).findById(2L);
    }
}
