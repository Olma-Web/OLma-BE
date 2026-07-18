package com.olma.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "프로젝트명 수정 요청")
public class ProjectNameUpdateRequest {

    @Size(max = 100)
    @Schema(description = "새 프로젝트명. 비어 있으면 생성일 기반 기본 이름을 사용합니다.", example = "쇼핑몰 관리자 페이지 구축", maxLength = 100)
    private String projectName;
}
