package com.soulsurf.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostsByBeachDTO {
    private Long beachId;
    private String beachName;
    private Long postsCount;
}


