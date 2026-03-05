package com.soulsurf.backend.modules.notification.mapper;

import com.soulsurf.backend.modules.notification.dto.NotificationDTO;
import com.soulsurf.backend.modules.notification.entity.Notification;
import org.springframework.stereotype.Component;
import com.soulsurf.backend.modules.user.mapper.UserMapper;

@Component
public class NotificationMapper {

    private final UserMapper userMapper;

    public NotificationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setSender(userMapper.toMentionDto(notification.getSender()));
        dto.setType(notification.getType().name());

        if (notification.getPost() != null) {
            dto.setPostId(notification.getPost().getId());
        }
        if (notification.getComment() != null) {
            dto.setCommentId(notification.getComment().getId());
        }

        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());

        String message = switch (notification.getType()) {
            case MENTION -> notification.getSender().getUsername() + " mencionou você em um comentário";
            case COMMENT -> notification.getSender().getUsername() + " comentou em seu post";
            case REPLY -> notification.getSender().getUsername() + " respondeu ao seu comentário";
            case LIKE -> notification.getSender().getUsername() + " curtiu seu post";
        };
        dto.setMessage(message);

        return dto;
    }
}

