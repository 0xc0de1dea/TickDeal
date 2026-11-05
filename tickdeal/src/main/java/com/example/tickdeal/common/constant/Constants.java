package com.example.tickdeal.common.constant;

public class Constants {

    public static final String AUTH_HEADER = "Authorization";

    public static final String BEARER_PREFIX = "Bearer ";

    public static final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L; // 60분

    public static final long REFRESH_TOKEN_TIME = ACCESS_TOKEN_TIME * 24 * 14L; // 14일

    public static final String TOKEN_TYPE = "tokenType";

    public static final String ACCESS = "ACCESS";

    public static final String REFRESH = "REFRESH";
}
