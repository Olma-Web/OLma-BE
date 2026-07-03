package com.olma.dto;

import com.olma.domain.enums.CommunityCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class CommunityPostUpdateRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String content;

    @NotNull
    private CommunityCategory category;

    @Size(max = 3, message = "이미지는 최대 3장까지 첨부할 수 있습니다.")
    private List<@NotBlank @Size(max = 2048) String> imageUrls;
}
