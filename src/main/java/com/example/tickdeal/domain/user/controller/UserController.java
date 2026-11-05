package com.example.tickdeal.domain.user.controller;

import com.example.tickdeal.common.auth.dto.TokenResponse;
import com.example.tickdeal.common.dto.ApiResponse;
import com.example.tickdeal.domain.user.dto.UserDto;
import com.example.tickdeal.domain.user.dto.request.LoginRequest;
import com.example.tickdeal.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto.Response>> register(
            @Valid @RequestBody UserDto.Request request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(userService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(userService.login(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> findOne(
            @PathVariable long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(userService.findOne(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> update(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserDto.Request request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(userService.update(user, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user
    ) {
        userService.delete(user);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent());
    }
}
