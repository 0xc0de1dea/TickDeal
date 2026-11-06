package com.example.tickdeal.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * Validation
     */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청값 검증에 실패했습니다."),

    /**
     * Token
     */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 리프레쉬 토큰 입니다."),
    UNMATCHED_REFRESH_TOKEN(HttpStatus.CONFLICT, "리프레쉬 토큰이 일치하지 않습니다."),

    /**
     * Product
     */
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 상품을 찾을 수 없습니다."),

    /**
     * User
     */
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 유저를 찾을 수 없습니다."),
    USER_NOT_MATCHED(HttpStatus.CONFLICT, "권한이 없습니다."),
    USER_INVALID_ROLE(HttpStatus.UNAUTHORIZED, "잘못된 권한입니다.");

    private final HttpStatus status;
    private final String message;
}
