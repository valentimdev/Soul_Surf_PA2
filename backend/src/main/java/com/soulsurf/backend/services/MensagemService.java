package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.MensagemDTO;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.Beach;
import com.soulsurf.backend.entities.Mensagem;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.BeachRepository;
import com.soulsurf.backend.repository.MensagemRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final BeachRepository beachRepository;
    private final UserRepository userRepository;

    // Você precisará de injeção de dependências para PraiaRepository e UserRepository
    public MensagemService(MensagemRepository mensagemRepository, BeachRepository praiaRepository, UserRepository userRepository) {
        this.mensagemRepository = mensagemRepository;
        this.beachRepository = praiaRepository;
        this.userRepository = userRepository;
    }

    // Método para listar mensagens (GET)
    public List<MensagemDTO> listarMensagensPorPraia(Long praiaId) {
        // Busca a praia primeiro
        Beach praia = beachRepository.findById(praiaId)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada."));

        // Busca e ordenação feitas pelo método do JpaRepository
        List<Mensagem> mensagens = mensagemRepository.findByBeachOrderByDataDesc(praia);

        return mensagens.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Método para criar uma nova mensagem (POST - PROTEGIDO)
    @Transactional
    public MensagemDTO criarMensagem(Long praiaId, String texto, String userEmail) {
        // Busca o usuário logado (assumindo que userEmail é o identificador único)
        User autor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado."));

        // Busca a praia onde a mensagem será postada
        Beach praia = beachRepository.findById(praiaId)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada."));

        Mensagem mensagem = new Mensagem();
        mensagem.setTexto(texto);
        mensagem.setAutor(autor);
        mensagem.setBeach(praia);

        mensagem = mensagemRepository.save(mensagem);
        return convertToDto(mensagem);
    }

    // Conversor de Entidade para DTO (Adaptar UserDTO se tiver mais campos)
    private MensagemDTO convertToDto(Mensagem mensagem) {
        MensagemDTO dto = new MensagemDTO();
        dto.setId(mensagem.getId());
        dto.setTexto(mensagem.getTexto());
        dto.setData(mensagem.getData());
        dto.setPraiaId(mensagem.getBeach().getId());

        // Mapeamento básico do autor
        UserDTO userDTO = new UserDTO();
        userDTO.setId(mensagem.getAutor().getId());
        userDTO.setUsername(mensagem.getAutor().getUsername());
        userDTO.setEmail(mensagem.getAutor().getEmail());
        // Adicione fotoPerfil, etc., se UserDTO tiver esses campos

        dto.setAutor(userDTO);

        return dto;
    }
}