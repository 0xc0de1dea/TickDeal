package com.example.tickdeal.common.utils;

import com.example.tickdeal.common.enums.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static com.example.tickdeal.common.constant.Constants.*;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKeyString;

    private SecretKey key;
    private JwtParser parser;

    @PostConstruct
    public void init() {
        byte[] bytes = Decoders.BASE64.decode(secretKeyString);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.parser = Jwts.parser()
                .verifyWith(this.key)
                .build();
    }

    public String generateAccessToken(
            String email,
            UserRoleEnum role
    ) {
        return generateToken(email, role, ACCESS, ACCESS_TOKEN_TIME);
    }

    public String generateRefreshToken(
            String email,
            UserRoleEnum role
    ) {
        return generateToken(email, role, REFRESH, REFRESH_TOKEN_TIME);
    }

    private String generateToken(
            String email,
            UserRoleEnum role,
            String tokenType,
            long expirationTime
    ) {
        Date now = new Date();

        return BEARER_PREFIX + Jwts.builder()
                .subject(email)
                .claim("email", email)
                .claim("role", role.name())
                .claim(TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    public boolean validateAccessToken(String token) {
        return validateTokenType(token, ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, REFRESH);
    }

    private boolean validateTokenType(
            String token,
            String expectedType
    ) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            Claims claims = parseClaims(token);

            return expectedType.equals(
                    claims.get(TOKEN_TYPE, String.class)
            );
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.toString());
            return false;
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long getRefreshTokenExpirationMillis() {
        return REFRESH_TOKEN_TIME;
    }
}
