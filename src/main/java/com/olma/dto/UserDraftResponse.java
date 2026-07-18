package com.olma.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.olma.domain.enums.UserDraftStatus;
import com.olma.domain.enums.UserDraftType;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class UserDraftResponse {
    private Long id;
    private UserDraftType type;
    private UserDraftStatus status;
    private JsonNode state;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime completedAt;
}
