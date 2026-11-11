package com.example.tickdeal.domain.notification.dto;

import com.example.tickdeal.domain.order.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderNotificationDto {

    private String orderNumber;

    private OrderStatus status;

    private String message;

    private LocalDateTime createdAt;
}
