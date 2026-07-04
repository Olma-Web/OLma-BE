package com.olma.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponseCreateParams;
import com.olma.config.OpenAiProperties;
import com.olma.dto.AiEstimateAnalysis;
import com.olma.dto.AiEstimateCalculateRequest;
import com.olma.exception.AiEstimateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OpenAiEstimateAnalyzer implements AiEstimateAnalyzer {

    private final OpenAiProperties properties;

    @Override
    public AiEstimateAnalysis analyze(AiEstimateCalculateRequest request) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AiEstimateException("OPENAI_API_KEY가 설정되어 있지 않습니다.");
        }
        try {
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(properties.getApiKey())
                    .timeout(Duration.ofSeconds(45))
                    .build();

            StructuredResponseCreateParams<AiEstimateAnalysis> params = ResponseCreateParams.builder()
                    .input(buildPrompt(request))
                    .text(AiEstimateAnalysis.class)
                    .model(ChatModel.of(properties.getModel()))
                    .build();

            return client.responses().create(params).output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .findFirst()
                    .orElseThrow(() -> new AiEstimateException("AI 견적 분석 결과가 비어 있습니다."));
        } catch (AiEstimateException e) {
            throw e;
        } catch (Exception e) {
            throw new AiEstimateException("AI 견적 분석에 실패했습니다.", e);
        }
    }

    private String buildPrompt(AiEstimateCalculateRequest request) {
        return """
                당신은 UI/UX 디자인 견적 분석가입니다.
                사용자의 자연어 요구사항을 UI/UX 디자인 프로젝트 관점으로만 분석하세요.
                개발 구현 공수가 아니라 화면 설계, UX 플로우, UI 디자인, 프로토타입, QA, 핸드오프 산출물 중심으로 분해하세요.
                각 feature의 complexity는 LOW, MEDIUM, HIGH 중 하나만 사용하세요.
                minDays <= expectedDays <= maxDays를 만족해야 하며, 모든 day 값은 1 이상의 정수여야 합니다.
                화면 수는 주요 화면, 상태 화면, 관리자/설정 화면을 포함해 현실적으로 추정하세요.

                프로젝트 설명:
                %s

                플랫폼 힌트: %s
                희망 일정: %s
                예산: %s
                범위 힌트: %s
                """.formatted(
                request.getProjectDescription(),
                blankToDash(request.getPlatformHint()),
                request.getDesiredTimelineDays() != null ? request.getDesiredTimelineDays() + "일" : "-",
                request.getBudgetAmount() != null ? request.getBudgetAmount() + "원" : "-",
                blankToDash(request.getScopeHint()));
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
