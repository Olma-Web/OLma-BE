package com.olma.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthLoginResponse {

    private Long id;
    
    private String token;

}
