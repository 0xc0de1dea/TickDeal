package com.example.tickdeal.domain.order.controller;

import com.example.tickdeal.common.dto.ApiResponse;
import com.example.tickdeal.domain.order.dto.OrderDto;
import com.example.tickdeal.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto.Response>> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderDto.Request request
    ) {

        OrderDto.Response response =
                orderService.createOrder(user, request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.created(response));
    }
}