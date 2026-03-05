package com.soulsurf.backend.modules.comment.mapper;

import com.soulsurf.backend.modules.comment.dto.CommentDTO;
import com.soulsurf.backend.modules.comment.entity.Comment;
import org.springframework.stereotype.Component;
import com.soulsurf.backend.modules.user.mapper.UserMapper;

import java.util.stream.Collectors;

@Component
public class CommentMapper {

    private final UserMapper userMapper;

    public CommentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CommentDTO toDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setTexto(comment.getTexto());
        dto.setData(comment.getData());

        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        dto.setUsuario(userMapper.toSimpleDto(comment.getUsuario()));

        dto.setReplies(comment.getReplies().stream()
                .map(this::toDto)
                .collect(Collectors.toList()));

        return dto;
    }
}

