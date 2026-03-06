package com.soulsurf.backend.modules.beach.dto;

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



