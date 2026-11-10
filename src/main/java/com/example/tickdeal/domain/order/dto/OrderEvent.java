package com.example.tickdeal.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String orderNumber;

    private Long userId;

    private Long dealId;

    private Integer quantity;

    private Long orderPrice;

    private LocalDateTime createdAt;
}
