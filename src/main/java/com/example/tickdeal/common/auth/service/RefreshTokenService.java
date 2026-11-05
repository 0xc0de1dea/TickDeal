package com.example.tickdeal.common.auth.service;

import com.example.tickdeal.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.example.tickdeal.common.constant.Constants.BEARER_PREFIX;
import static com.example.tickdeal.common.constant.Constants.REFRESH_KEY_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public void save(String email, String refreshToken) {
        refreshToken = refreshToken.substring(BEARER_PREFIX.length());

        redisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + email,
                refreshToken,
                jwtUtil.getRefreshTokenExpirationMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    public String findByEmail(String email) {
        return redisTemplate.opsForValue()
                .get(REFRESH_KEY_PREFIX + email);
    }

    public boolean matches(String email, String refreshToken) {
        String savedToken = findByEmail(email);

        return savedToken != null
                && savedToken.equals(refreshToken);
    }

    public void delete(String email) {
        redisTemplate.delete(REFRESH_KEY_PREFIX + email);
    }
}
