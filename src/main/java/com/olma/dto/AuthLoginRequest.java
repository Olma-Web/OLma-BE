package com.olma.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AuthLoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
