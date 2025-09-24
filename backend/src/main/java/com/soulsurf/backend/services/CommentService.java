package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.CommentDTO;
import com.soulsurf.backend.dto.UserDTO;
import com.soulsurf.backend.entities.Comment;
import com.soulsurf.backend.entities.Post;
import com.soulsurf.backend.entities.User;
import com.soulsurf.backend.repository.CommentRepository;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public CommentDTO createComment(Long postId, String texto, String userEmail) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        Comment comment = new Comment();
        comment.setTexto(texto);
        comment.setUsuario(usuario);
        comment.setPost(post);

        comment = commentRepository.save(comment);
        return convertToDto(comment);
    }

    public List<CommentDTO> getPostComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        return post.getComments().stream()
                .sorted((c1, c2) -> c2.getData().compareTo(c1.getData()))  // ordenar por data, mais recentes primeiro
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CommentDTO convertToDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setTexto(comment.getTexto());
        dto.setData(comment.getData());

        var userDTO = new UserDTO();
        userDTO.setId(comment.getUsuario().getId());
        userDTO.setUsername(comment.getUsuario().getUsername());
        userDTO.setEmail(comment.getUsuario().getEmail());
        dto.setUsuario(userDTO);

        return dto;
    }
}