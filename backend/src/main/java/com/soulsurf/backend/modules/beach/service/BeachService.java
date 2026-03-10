package com.soulsurf.backend.modules.beach.service;

import com.soulsurf.backend.modules.beach.dto.BeachDTO;
import com.soulsurf.backend.modules.post.dto.PostDTO;
import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.beach.mapper.BeachMapper;
import com.soulsurf.backend.modules.post.mapper.PostMapper;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
import com.soulsurf.backend.modules.post.repository.PostRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.soulsurf.backend.core.storage.OracleStorageService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BeachService {

    private final BeachRepository beachRepository;
    private final PostRepository postRepository;
    private final Optional<OracleStorageService> blobStorageService;
    private final BeachMapper beachMapper;
    private final PostMapper postMapper;

    public BeachService(BeachRepository beachRepository, PostRepository postRepository,
            Optional<OracleStorageService> blobStorageService,
            BeachMapper beachMapper, @Lazy PostMapper postMapper) {
        this.beachRepository = beachRepository;
        this.postRepository = postRepository;
        this.blobStorageService = blobStorageService;
        this.beachMapper = beachMapper;
        this.postMapper = postMapper;
    }

    public Beach createBeach(String nome, String descricao, String localizacao, String nivelExperiencia,
            MultipartFile foto) {
        Beach beach = new Beach();
        beach.setNome(nome);
        beach.setDescricao(descricao);
        beach.setLocalizacao(localizacao);
        beach.setNivelExperiencia(nivelExperiencia);

        if (blobStorageService.isPresent() && foto != null && !foto.isEmpty()) {
            try {
                String urlDaFoto = blobStorageService.get().uploadFile(foto);
                beach.setCaminhoFoto(urlDaFoto);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao fazer upload da foto da praia: " + e.getMessage(), e);
            }
        }

        return beachRepository.save(beach);
    }

    public List<BeachDTO> getAllBeaches() {
        return beachRepository.findAll().stream()
                .map(beachMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<BeachDTO> getBeachById(Long id) {
        return beachRepository.findById(id)
                .map(beachMapper::toDto);
    }

    @Cacheable(value = "beachPosts", key = "#beachId + '_' + #page + '_' + #size + '_' + (#currentUserEmail ?: 'anonymous')")
    public List<PostDTO> getBeachPosts(Long beachId, int page, int size, String currentUserEmail) {
        Beach beach = beachRepository.findById(beachId)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada"));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("data").descending());

        List<Post> posts;
        if (currentUserEmail != null) {
            posts = postRepository.findByBeachAndPublicoIsTrueOrUsuarioEmail(beach, currentUserEmail, pageRequest)
                    .getContent();
        } else {
            posts = postRepository.findByBeachAndPublicoIsTrue(beach, pageRequest).getContent();
        }

        return posts.stream()
                .map(post -> postMapper.toDto(post, currentUserEmail))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "beachPosts", key = "#beachId + '_all_' + (#userEmail ?: 'anonymous')")
    public List<PostDTO> getAllBeachPosts(Long beachId, String userEmail) {
        Beach beach = beachRepository.findById(beachId)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada"));

        return postRepository.findByBeachOrderByDataDesc(beach).stream()
                .map(post -> postMapper.toDto(post, userEmail))
                .collect(Collectors.toList());
    }
}
