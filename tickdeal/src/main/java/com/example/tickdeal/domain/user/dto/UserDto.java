package com.example.tickdeal.domain.user.dto;

import com.example.tickdeal.common.enums.UserRoleEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class UserDto {

    @Getter
    @AllArgsConstructor
    public static class Request {

        private String name;

        private String email;

        private String password;

        private UserRoleEnum role;
    }

    @Getter
    @AllArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"name", "email", "role", "createdAt", "modifiedAt"})
    public static class Response {

        private String name;

        private String email;

        private UserRoleEnum role;

        private LocalDateTime createdAt;

        private LocalDateTime modifiedAt;

        public static Response build(
                String name,
                String email,
                UserRoleEnum role,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt
        ) {
            return Response.builder()
                    .name(name)
                    .email(email)
                    .role(role)
                    .createdAt(createdAt)
                    .modifiedAt(modifiedAt)
                    .build();
        }
    }
}
