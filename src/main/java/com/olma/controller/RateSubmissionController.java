package com.olma.controller;

import com.olma.dto.ProjectNameUpdateRequest;
import com.olma.dto.RateSubmissionRequest;
import com.olma.dto.RateSubmissionResponse;
import com.olma.service.RateSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Rate Submission", description = "직무/경력/근무 형태별 단가 제보 API")
public class RateSubmissionController {

    private final RateSubmissionService rateSubmissionService;

    @Operation(
            summary = "단가 제보 생성",
            description = "직무, 경력, 근무 형태, 금액 정보를 받아 단가 제보를 생성합니다. userId가 없으면 JWT에서 추출한 userId를 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "단가 제보 생성 성공",
                    content = @Content(schema = @Schema(implementation = RateSubmissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "필수 필드 누락, 잘못된 기준 데이터 ID, 금액 범위 오류", content = @Content),
            @ApiResponse(responseCode = "401", description = "JWT 미제공 또는 유효하지 않은 토큰", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RateSubmissionResponse create(@Valid @RequestBody RateSubmissionRequest request,
                                         HttpServletRequest httpRequest) {
        if (request.getUserId() == null) {
            request.setUserId((Long) httpRequest.getAttribute("userId"));
        }
        return rateSubmissionService.create(request);
    }

    @Operation(
            summary = "단가 제보 단건 조회",
            description = "ID로 ACTIVE 상태의 단가 제보를 조회합니다. HIDDEN 상태의 제보는 404를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "단가 제보 조회 성공",
                    content = @Content(schema = @Schema(implementation = RateSubmissionResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT 미제공 또는 유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 제보가 없음", content = @Content)
    })
    @GetMapping("/{id}")
    public RateSubmissionResponse getById(
            @Parameter(description = "단가 제보 ID", example = "1")
            @PathVariable Long id) {
        return rateSubmissionService.getById(id);
    }

    @Operation(
            summary = "단가 제보 프로젝트명 수정",
            description = "본인이 생성한 ACTIVE 상태의 단가 제보 프로젝트명을 수정합니다. 소유자가 아니면 403을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트명 수정 성공",
                    content = @Content(schema = @Schema(implementation = RateSubmissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "프로젝트명이 최대 길이를 초과함", content = @Content),
            @ApiResponse(responseCode = "401", description = "JWT 미제공 또는 유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "403", description = "요청자가 제보 소유자가 아님", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 ACTIVE 제보가 없음", content = @Content)
    })
    @PatchMapping("/{id}/project-name")
    public RateSubmissionResponse updateProjectName(@Parameter(description = "단가 제보 ID", example = "1")
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody ProjectNameUpdateRequest request,
                                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return rateSubmissionService.updateProjectName(userId, id, request);
    }

    @Operation(
            summary = "단가 제보 소프트 삭제",
            description = "본인이 생성한 ACTIVE 상태의 단가 제보를 HIDDEN으로 변경합니다. 소유자가 아니면 403을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "소프트 삭제 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "JWT 미제공 또는 유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "403", description = "요청자가 제보 소유자가 아님", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 ACTIVE 제보가 없음", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "단가 제보 ID", example = "1")
                       @PathVariable Long id,
                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        rateSubmissionService.delete(userId, id);
    }
}
