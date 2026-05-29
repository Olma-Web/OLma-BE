package com.olma.controller;

import com.olma.domain.entity.ExperienceLevel;
import com.olma.domain.entity.JobCategory;
import com.olma.domain.entity.Region;
import com.olma.domain.entity.WorkType;
import com.olma.domain.repository.CertificateTypeRepository;
import com.olma.domain.repository.ExperienceLevelRepository;
import com.olma.domain.repository.JobCategoryRepository;
import com.olma.domain.repository.RegionRepository;
import com.olma.domain.repository.WorkTypeRepository;
import com.olma.dto.JobCategoryResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/reference")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class ReferenceDataController {

    private final JobCategoryRepository jobCategoryRepository;
    private final WorkTypeRepository workTypeRepository;
    private final RegionRepository regionRepository;
    private final ExperienceLevelRepository experienceLevelRepository;
    private final CertificateTypeRepository certificateTypeRepository;

    @GetMapping("/job-categories")
    @Transactional(readOnly = true)
    public List<JobCategoryResponse> getJobCategories() {
        List<JobCategory> roots = jobCategoryRepository.findRootsWithChildren();
        return roots.stream().map(this::toResponse).toList();
    }

    @GetMapping("/work-types")
    public List<Map<String, Object>> getWorkTypes() {
        return workTypeRepository.findAllByOrderByDisplayOrder().stream()
                .map(wt -> Map.<String, Object>of("id", wt.getId(), "name", wt.getName()))
                .toList();
    }

    @GetMapping("/regions")
    public List<Map<String, Object>> getRegions() {
        return regionRepository.findAllByOrderByDisplayOrder().stream()
                .map(r -> Map.<String, Object>of("id", r.getId(), "name", r.getName()))
                .toList();
    }

    @GetMapping("/experience-levels")
    public List<Map<String, Object>> getExperienceLevels() {
        return experienceLevelRepository.findAllByOrderByDisplayOrder().stream()
                .map(el -> Map.<String, Object>of(
                        "id", el.getId(),
                        "label", el.getLabel(),
                        "minYears", el.getMinYears(),
                        "maxYears", el.getMaxYears()))
                .toList();
    }

    @GetMapping("/certificate-types")
    public List<Map<String, Object>> getCertificateTypes() {
        return certificateTypeRepository.findAll().stream()
                .map(ct -> Map.<String, Object>of("id", ct.getId(), "name", ct.getName()))
                .toList();
    }

    private JobCategoryResponse toResponse(JobCategory category) {
        List<JobCategoryResponse> children = category.getChildren().stream()
                .filter(JobCategory::getIsActive)
                .map(this::toResponse)
                .toList();
        return JobCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .children(children.isEmpty() ? null : children)
                .build();
    }
}
