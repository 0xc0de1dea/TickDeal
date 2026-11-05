package com.example.tickdeal.common.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshRequest {

    private String refreshToken;
}
