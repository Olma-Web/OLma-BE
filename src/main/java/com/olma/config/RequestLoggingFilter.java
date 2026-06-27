package com.olma.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        MDC.put("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startMs = System.currentTimeMillis();
        try {
            chain.doFilter(req, res);
        } finally {
            long durationMs = System.currentTimeMillis() - startMs;
            int status = response.getStatus();
            String userId = MDC.get("userId") != null ? MDC.get("userId") : "anonymous";

            if (status >= 500) {
                log.error("request failed method={} path={} status={} durationMs={} userId={}",
                        request.getMethod(), request.getRequestURI(), status, durationMs, userId);
            } else if (status >= 400) {
                log.warn("request completed method={} path={} status={} durationMs={} userId={}",
                        request.getMethod(), request.getRequestURI(), status, durationMs, userId);
            } else {
                log.info("request completed method={} path={} status={} durationMs={} userId={}",
                        request.getMethod(), request.getRequestURI(), status, durationMs, userId);
            }

            MDC.clear();
        }
    }
}
