package com.soulsurf.backend.services;

import com.soulsurf.backend.dto.AdminMetricsDTO;
import com.soulsurf.backend.dto.AdminPeriodMetricsDTO;
import com.soulsurf.backend.dto.PostsByBeachDTO;
import com.soulsurf.backend.dto.TopAuthorDTO;
import com.soulsurf.backend.repository.CommentRepository;
import com.soulsurf.backend.repository.PostRepository;
import com.soulsurf.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminMetricsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public AdminMetricsService(UserRepository userRepository,
                               PostRepository postRepository,
                               CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public AdminMetricsDTO getMetrics() {
        AdminMetricsDTO dto = new AdminMetricsDTO();
        dto.setTotalUsers(userRepository.count());
        dto.setTotalAdmins(userRepository.countByAdminTrue());
        dto.setTotalBannedUsers(userRepository.countByBannedTrue());
        dto.setTotalPosts(postRepository.count());
        dto.setTotalComments(commentRepository.count());
        dto.setActiveAuthors(postRepository.countDistinctAuthors());
        return dto;
    }

    public AdminPeriodMetricsDTO getPeriodMetrics(LocalDateTime start, LocalDateTime end) {
        AdminPeriodMetricsDTO dto = new AdminPeriodMetricsDTO();
        dto.setPostsCount(postRepository.countPostsBetween(start, end));
        dto.setCommentsCount(commentRepository.countCommentsBetween(start, end));
        // usuários ativos = autores com posts no período
        dto.setActiveUsersCount(postRepository.topAuthorsBetween(start, end, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).size());
        return dto;
    }

    public List<TopAuthorDTO> getTopAuthors(LocalDateTime start, LocalDateTime end, int limit) {
        var rows = postRepository.topAuthorsBetween(start, end, org.springframework.data.domain.PageRequest.of(0, limit));
        return rows.stream()
                .map(r -> new TopAuthorDTO((Long) r[0], (String) r[1], (Long) r[2]))
                .collect(Collectors.toList());
    }

    public List<PostsByBeachDTO> getPostsByBeach(LocalDateTime start, LocalDateTime end) {
        var rows = postRepository.postsByBeachBetween(start, end);
        return rows.stream()
                .map(r -> new PostsByBeachDTO((Long) r[0], (String) r[1], (Long) r[2]))
                .collect(Collectors.toList());
    }
}


