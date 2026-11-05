package com.example.tickdeal.common.auth.service;

import com.example.tickdeal.common.auth.dto.TokenResponse;
import com.example.tickdeal.common.enums.UserRoleEnum;
import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public TokenResponse reissue(String refreshToken) {

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new ServiceException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        String email = jwtUtil.extractEmail(refreshToken);

        if (!refreshTokenService.matches(email, refreshToken)) {
            throw new ServiceException(
                    ErrorCode.UNMATCHED_REFRESH_TOKEN
            );
        }

        UserRoleEnum role;

        try {
            role = UserRoleEnum.valueOf(
                    jwtUtil.extractRole(refreshToken)
            );
        } catch (IllegalArgumentException e) {
            throw new ServiceException(
                    ErrorCode.USER_INVALID_ROLE
            );
        }

        String newAccessToken =
                jwtUtil.generateAccessToken(email, role);

        String newRefreshToken =
                jwtUtil.generateRefreshToken(email, role);

        refreshTokenService.save(email, newRefreshToken);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    public void logout(String refreshToken) {

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new ServiceException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        String email = jwtUtil.extractEmail(refreshToken);

        if (!refreshTokenService.matches(email, refreshToken)) {
            throw new ServiceException(
                    ErrorCode.UNMATCHED_REFRESH_TOKEN
            );
        }

        refreshTokenService.delete(email);
    }
}
