package com.soulsurf.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopAuthorDTO {
    private Long userId;
    private String username;
    private Long postsCount;
}


