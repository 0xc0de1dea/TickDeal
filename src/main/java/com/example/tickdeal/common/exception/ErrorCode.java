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
    PRODUCT_IS_DE_ACTIVE(HttpStatus.BAD_REQUEST, "비활성 상품은 딜을 생성할 수 없습니다."),

    /**
     * Deal
     */
    DEAL_PRICE_NOT_ALLOWED(HttpStatus.CONFLICT, "딜 가격은 0보다 커야 합니다."),
    DEAL_STOCK_NOT_ALLOWED(HttpStatus.CONFLICT, "재고는 0보다 커야 합니다."),
    DEAL_END_TIME_NOT_ALLOWED(HttpStatus.CONFLICT, "딜 종료 시간은 시작 시간보다 늦어야 합니다."),
    DEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 딜을 찾을 수 없습니다."),
    DEAL_ALREADY_IN_PROGRESS(HttpStatus.BAD_REQUEST, "진행 중이거나 종료된 딜은 수정할 수 없습니다."),

    /**
     * Stock
     */
    STOCK_INFORMATION_NOT_EXISTS_REDIS(HttpStatus.NOT_FOUND, "Redis에 재고 정보가 존재하지 않습니다."),
    STOCK_DECREASE_RESULT_NOT_EXISTS_REDIS(HttpStatus.NOT_FOUND, "Redis 재고 차감 결과가 없습니다."),
    STOCK_RESTORE_RESULT_NOT_EXISTS_REDIS(HttpStatus.NOT_FOUND, "Redis 복구 결과가 없습니다."),
    STOCK_ALREADY_INITIATED_REDIS(HttpStatus.CONFLICT, "이미 Redis 재고가 초기화되어 있습니다."),

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
