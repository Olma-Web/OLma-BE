package com.olma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommunityCommentUpdateRequest {

    @NotBlank
    @Size(max = 3000)
    private String content;
}
