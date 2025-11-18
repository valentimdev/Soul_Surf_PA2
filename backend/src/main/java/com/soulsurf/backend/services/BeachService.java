package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.BeachDTO;
import com.soulsurf.backend.dto.PostDTO;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.Beach;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.repository.BeachRepository;
import com.soulsurf.backend.repository.PostRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BeachService {

    private final BeachRepository beachRepository;
    private final PostRepository postRepository;
    private final Optional<BlobStorageService> blobStorageService;

    public BeachService(BeachRepository beachRepository, PostRepository postRepository, Optional<BlobStorageService> blobStorageService) {
        this.beachRepository = beachRepository;
        this.postRepository = postRepository;
        this.blobStorageService = blobStorageService;
    }

    public Beach createBeach(String nome, String descricao, String localizacao, String nivelExperiencia, MultipartFile foto) throws IOException {
        Beach beach = new Beach();
        beach.setNome(nome);
        beach.setDescricao(descricao);
        beach.setLocalizacao(localizacao);
        beach.setNivelExperiencia(nivelExperiencia);

        if (blobStorageService.isPresent() && foto != null && !foto.isEmpty()) {
            String urlDaFoto = blobStorageService.get().uploadFile(foto);
            beach.setCaminhoFoto(urlDaFoto);
        }

        return beachRepository.save(beach);
    }

    public List<BeachDTO> getAllBeaches() {
        return beachRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<BeachDTO> getBeachById(Long id) {
        return beachRepository.findById(id)
                .map(this::convertToDto);
    }
    @Cacheable(value = "beachPosts", key = "#beachId + '_' + #page + '_' + #size + '_' + (#currentUserEmail ?: 'anonymous')")
    public List<PostDTO> getBeachPosts(Long beachId, int page, int size, String currentUserEmail) {
        Beach beach = beachRepository.findById(beachId)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada"));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("data").descending());

        if (currentUserEmail != null) {
            // Se está logado, busca posts públicos ou posts próprios
            return postRepository.findByBeachAndPublicoIsTrueOrUsuarioEmail(beach, currentUserEmail, pageRequest)
                    .stream()
                    .map(this::convertPostToDto)
                    .collect(Collectors.toList());
        } else {
            // Se não está logado, busca apenas posts públicos
            return postRepository.findByBeachAndPublicoIsTrue(beach, pageRequest)
                    .stream()
                    .map(this::convertPostToDto)
                    .collect(Collectors.toList());
        }
    }
    @Cacheable(value = "beachPosts", key = "#beachId + '_all_' + (#userEmail ?: 'anonymous')")
    public List<PostDTO> getAllBeachPosts(Long beachId, String userEmail) {
        Beach beach = beachRepository.findById(beachId)
                .orElseThrow(() -> new RuntimeException("Praia não encontrada"));

        return postRepository.findByBeachOrderByDataDesc(beach).stream()
                .map(this::convertPostToDto)
                .collect(Collectors.toList());
    }

    private BeachDTO convertToDto(Beach beach) {
        BeachDTO beachDTO = new BeachDTO();
        beachDTO.setId(beach.getId());
        beachDTO.setNome(beach.getNome());
        beachDTO.setDescricao(beach.getDescricao());
        beachDTO.setLocalizacao(beach.getLocalizacao());
        beachDTO.setCaminhoFoto(beach.getCaminhoFoto());
        beachDTO.setNivelExperiencia(beach.getNivelExperiencia());
        return beachDTO;
    }

    // Método auxiliar para converter Post para PostDTO
    private PostDTO convertPostToDto(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setPublico(post.isPublico());
        postDTO.setDescricao(post.getDescricao());
        postDTO.setCaminhoFoto(post.getCaminhoFoto());
        postDTO.setData(post.getData());

        // Converter usuário
        UserDTO userDTO = new UserDTO();
        userDTO.setId(post.getUsuario().getId());
        userDTO.setUsername(post.getUsuario().getUsername());
        userDTO.setEmail(post.getUsuario().getEmail());
        postDTO.setUsuario(userDTO);

        // Converter praia
        BeachDTO beachDTO = new BeachDTO();
        beachDTO.setId(post.getBeach().getId());
        beachDTO.setNome(post.getBeach().getNome());
        beachDTO.setDescricao(post.getBeach().getDescricao());
        beachDTO.setLocalizacao(post.getBeach().getLocalizacao());
        beachDTO.setCaminhoFoto(post.getBeach().getCaminhoFoto());
        postDTO.setBeach(beachDTO);

        return postDTO;
    }
}