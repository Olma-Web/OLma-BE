package com.olma.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class AiEstimateAnalysis {
    @JsonPropertyDescription("A short Korean project title inferred from the request.")
    public String projectTitle;

    @JsonPropertyDescription("The most likely UI/UX platform, for example 반응형 웹, 모바일 앱, 관리자 웹, PC 웹.")
    public String platform;

    @JsonPropertyDescription("Estimated number of meaningful design screens including key states.")
    public Integer estimatedScreenCount;

    @JsonPropertyDescription("Feature or workstream breakdown for this UI/UX design estimate.")
    public List<AiEstimateFeatureSeed> features;

    @JsonPropertyDescription("A concise Korean explanation that can be shown to a client.")
    public String clientMessage;

    public static class AiEstimateFeatureSeed {
        @JsonPropertyDescription("Korean feature or workstream name.")
        public String name;

        @JsonPropertyDescription("One sentence explaining the design work.")
        public String description;

        @JsonPropertyDescription("One of LOW, MEDIUM, HIGH.")
        public String complexity;

        @JsonPropertyDescription("Optimistic design work days.")
        public Integer minDays;

        @JsonPropertyDescription("Most likely design work days.")
        public Integer expectedDays;

        @JsonPropertyDescription("Pessimistic design work days.")
        public Integer maxDays;

        @JsonPropertyDescription("Primary role category: PLANNING, UX, UI, QA, HANDOFF.")
        public String role;

        @JsonPropertyDescription("Main deliverable produced by this workstream.")
        public String deliverable;
    }
}
