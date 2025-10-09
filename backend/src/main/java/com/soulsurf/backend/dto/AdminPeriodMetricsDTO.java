package com.soulsurf.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminPeriodMetricsDTO {
    private long postsCount;
    private long commentsCount;
    private long activeUsersCount;
}


