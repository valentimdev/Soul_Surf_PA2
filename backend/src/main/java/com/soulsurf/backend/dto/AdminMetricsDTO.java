package com.soulsurf.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminMetricsDTO {
    private long totalUsers;
    private long totalAdmins;
    private long totalBannedUsers;
    private long totalPosts;
    private long totalComments;
    private long activeAuthors; // distintos que postaram ao menos 1 post
}


