package com.olma.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String TOKEN_VERSION_CLAIM = "ver";

    private final SecretKey key;
    private final long expiration;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") Long expiration) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    public String generateJwtToken(Long userId, int tokenVersion) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TOKEN_VERSION_CLAIM, tokenVersion)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 서명과 만료를 검증한 뒤 클레임을 반환한다. 둘 중 하나라도 실패하면 JwtException을 던진다.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    /**
     * token_version 도입 이전에 발급된 토큰에는 클레임이 없어 null을 반환한다.
     */
    public Integer getTokenVersion(Claims claims) {
        return claims.get(TOKEN_VERSION_CLAIM, Integer.class);
    }
}
