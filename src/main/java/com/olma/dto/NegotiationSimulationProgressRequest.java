package com.olma.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NegotiationSimulationProgressRequest {
    @NotNull
    private JsonNode state;
}
