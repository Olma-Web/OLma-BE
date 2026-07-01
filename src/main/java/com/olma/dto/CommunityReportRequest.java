package com.olma.dto;

import com.olma.domain.enums.CommunityReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommunityReportRequest {

    @NotNull
    private CommunityReportReason reason;

    @Size(max = 1000)
    private String detail;
}
