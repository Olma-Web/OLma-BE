package com.olma.controller;

import com.olma.dto.AuthSignupRequest;
import com.olma.dto.AuthSignupResponse;
import com.olma.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public AuthSignupResponse signUp(@Valid @RequestBody AuthSignupRequest request) {
        return authService.signup(request);
    }
}
