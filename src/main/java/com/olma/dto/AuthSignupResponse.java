package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Builder
@Getter
public class AuthSignupResponse {

    private Long id;

    private String email;

    private String nickname;

    private OffsetDateTime agreementAt;

    private String token;
}
