package com.soulsurf.backend.modules.beach.service;

import com.soulsurf.backend.modules.beach.dto.BeachMessageDTO;
import com.soulsurf.backend.modules.user.dto.UserDTO;
import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.beach.entity.BeachMessage;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
import com.soulsurf.backend.modules.beach.repository.BeachMessageRepository;
import com.soulsurf.backend.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BeachMessageService {

    private final BeachMessageRepository BeachMessageRepository;
    private final BeachRepository beachRepository;
    private final UserRepository userRepository;

    // Você precisará de injeção de dependências para PraiaRepository e UserRepository
    public BeachMessageService(BeachMessageRepository BeachMessageRepository, BeachRepository praiaRepository, UserRepository userRepository) {
        this.BeachMessageRepository = BeachMessageRepository;
        this.beachRepository = praiaRepository;
        this.userRepository = userRepository;
    }

    // Método para listar mensagens (GET)
    public List<BeachMessageDTO> listarMensagensPorPraia(Long praiaId) {
        // Busca a praia primeiro
        Beach praia = beachRepository.findById(praiaId)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada."));

        // Busca e ordenação feitas pelo método do JpaRepository
        List<BeachMessage> mensagens = BeachMessageRepository.findByBeachOrderByDataDesc(praia);

        return mensagens.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Método para criar uma nova BeachMessage (POST - PROTEGIDO)
    @Transactional
    public BeachMessageDTO criarBeachMessage(Long praiaId, String texto, String userEmail) {
        // Busca o usuário logado (assumindo que userEmail é o identificador único)
        User autor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado."));

        // Busca a praia onde a BeachMessage será postada
        Beach praia = beachRepository.findById(praiaId)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada."));

        BeachMessage BeachMessage = new BeachMessage();
        BeachMessage.setTexto(texto);
        BeachMessage.setAutor(autor);
        BeachMessage.setBeach(praia);

        BeachMessage = BeachMessageRepository.save(BeachMessage);
        return convertToDto(BeachMessage);
    }

    // Conversor de Entidade para DTO (Adaptar UserDTO se tiver mais campos)
    private BeachMessageDTO convertToDto(BeachMessage BeachMessage) {
        BeachMessageDTO dto = new BeachMessageDTO();
        dto.setId(BeachMessage.getId());
        dto.setTexto(BeachMessage.getTexto());
        dto.setData(BeachMessage.getData());
        dto.setPraiaId(BeachMessage.getBeach().getId());

        // Mapeamento básico do autor
        UserDTO userDTO = new UserDTO();
        userDTO.setId(BeachMessage.getAutor().getId());
        userDTO.setUsername(BeachMessage.getAutor().getUsername());
        userDTO.setEmail(BeachMessage.getAutor().getEmail());
        // Adicione fotoPerfil, etc., se UserDTO tiver esses campos

        dto.setAutor(userDTO);

        return dto;
    }
}

