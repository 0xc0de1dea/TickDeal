package com.example.tickdeal.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRoleEnum {
    ADMIN("ROLE_ADMIN", "관리자 권한"),
    SELLER("ROLE_SELLER", "판매자 권한"),
    NORMAL("ROLE_NORMAL", "일반 사용자 권한");

    private final String role;
    private final String description;
}
