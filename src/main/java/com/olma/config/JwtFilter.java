package com.olma.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olma.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter implements Filter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    private static final List<String> PERMIT_PREFIXES = List.of(
            "/v1/auth/",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // CORS preflight requests carry no Authorization header; let Spring MVC handle them.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        if (PERMIT_PREFIXES.stream().anyMatch(prefix -> request.getRequestURI().startsWith(prefix))) {
            chain.doFilter(req, res);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeErrorResponse(response, request, HttpServletResponse.SC_UNAUTHORIZED, "인증 토큰이 없습니다.");
            return;
        }

        try {
            Long userId = jwtProvider.validateJwtToken(header.substring(7));
            request.setAttribute("userId", userId);
        } catch (JwtException e) {
            writeErrorResponse(response, request, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다.");
            return;
        }

        chain.doFilter(req, res);
    }

    private void writeErrorResponse(HttpServletResponse response, HttpServletRequest request, int status, String message) throws IOException {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .error("Unauthorized")
                .message(message)
                .path(request.getRequestURI())
                .build();

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
