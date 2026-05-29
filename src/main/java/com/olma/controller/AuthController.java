package com.olma.controller;

import com.olma.dto.AuthLoginRequest;
import com.olma.dto.AuthLoginResponse;
import com.olma.dto.AuthSignupRequest;
import com.olma.dto.AuthSignupResponse;
import com.olma.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public AuthSignupResponse signUp(@Valid @RequestBody AuthSignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthLoginResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "BearerAuth")
    public void logout() {
    }
}
