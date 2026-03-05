package com.soulsurf.backend.modules.post.mapper;

import com.soulsurf.backend.modules.post.dto.PostDTO;
import com.soulsurf.backend.modules.post.entity.Post;
import com.soulsurf.backend.modules.user.entity.User;
import com.soulsurf.backend.modules.post.service.LikeService;
import com.soulsurf.backend.modules.user.mapper.UserMapper;
import com.soulsurf.backend.modules.comment.mapper.CommentMapper;
import com.soulsurf.backend.modules.beach.mapper.BeachMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PostMapper {

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final BeachMapper beachMapper;
    private final LikeService likeService;

    public PostMapper(UserMapper userMapper,
            CommentMapper commentMapper,
            BeachMapper beachMapper,
            @Lazy LikeService likeService) {
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
        this.beachMapper = beachMapper;
        this.likeService = likeService;
    }

    public PostDTO toDto(Post post) {
        return toDto(post, null);
    }

    public PostDTO toDto(Post post, String currentUserEmail) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setPublico(post.isPublico());
        dto.setDescricao(post.getDescricao());
        dto.setCaminhoFoto(post.getCaminhoFoto());
        dto.setData(post.getData());

        User usuario = post.getUsuario();
        dto.setUsuario(userMapper.toDto(usuario));

        if (post.getBeach() != null) {
            dto.setBeach(beachMapper.toDto(post.getBeach()));
        }

        dto.setComments(post.getComments().stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(commentMapper::toDto)
                .collect(Collectors.toList()));

        dto.setLikesCount(likeService.countLikes(post.getId()));
        if (currentUserEmail != null) {
            dto.setLikedByCurrentUser(likeService.hasUserLiked(post.getId(), currentUserEmail));
        } else {
            dto.setLikedByCurrentUser(false);
        }
        dto.setCommentsCount(post.getComments().size());

        return dto;
    }
}

