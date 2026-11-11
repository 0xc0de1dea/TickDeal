package com.example.tickdeal.domain.order.controller;

import com.example.tickdeal.common.dto.ApiResponse;
import com.example.tickdeal.domain.order.dto.OrderDto;
import com.example.tickdeal.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDto.DetailResponse>> getOrder(
            @AuthenticationPrincipal User user,
            @PathVariable String orderNumber
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(orderService.getOrder(user, orderNumber)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ApiResponse.PageResponse<OrderDto.DetailResponse>>> getOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(orderService.getOrders(user, page, size)));
    }
}