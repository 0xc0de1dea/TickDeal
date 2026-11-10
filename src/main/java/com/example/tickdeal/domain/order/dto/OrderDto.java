package com.example.tickdeal.domain.order.dto;

import com.example.tickdeal.domain.order.entity.Order;
import com.example.tickdeal.domain.order.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class OrderDto {

    @Getter
    @AllArgsConstructor
    public static class Request {

        @NotNull(message = "딜 ID는 필수입니다.")
        private Long dealId;

        @NotNull(message = "주문 수량은 필수입니다.")
        @Min(value = 1, message = "주문 수량은 1개 이상이어야 합니다.")
        private Integer quantity;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"orderNumber", "status", "orderPrice", "quantity", "createdAt"})
    public static class Response {

        private String orderNumber;

        private OrderStatus status;

        private Long orderPrice;

        private Integer quantity;

        private LocalDateTime createdAt;

        public static Response build(
                String orderNumber,
                OrderStatus status,
                Long orderPrice,
                Integer quantity,
                LocalDateTime createdAt
        ) {
            return Response.builder()
                    .orderNumber(orderNumber)
                    .status(status)
                    .orderPrice(orderPrice)
                    .quantity(quantity)
                    .createdAt(createdAt)
                    .build();
        }

        public static Response from(Order order) {
            return Response.builder()
                    .orderNumber(order.getOrderNumber())
                    .status(order.getStatus())
                    .orderPrice(order.getOrderPrice())
                    .quantity(order.getQuantity())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }
}
