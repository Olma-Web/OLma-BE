package com.olma.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectNameUpdateRequest {

    @Size(max = 100)
    private String projectName;
}
