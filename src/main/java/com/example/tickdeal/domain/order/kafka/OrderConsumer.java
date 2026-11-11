package com.example.tickdeal.domain.order.kafka;

import com.example.tickdeal.domain.notification.dto.OrderNotificationDto;
import com.example.tickdeal.domain.notification.service.SseService;
import com.example.tickdeal.domain.order.dto.OrderEvent;
import com.example.tickdeal.domain.order.enums.OrderStatus;
import com.example.tickdeal.domain.order.service.OrderPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderPersistenceService orderPersistenceService;
    private final SseService sseService;

    @KafkaListener(
            topics = "tickdeal-order",
            groupId = "tickdeal-order-group"
    )
    @Transactional
    public void consume(OrderEvent event) {
        orderPersistenceService.saveOrder(event);

        OrderNotificationDto notification = OrderNotificationDto.builder()
                .orderNumber(event.getOrderNumber())
                .status(OrderStatus.COMPLETED)
                .message("주문이 완료되었습니다.")
                .createdAt(LocalDateTime.now())
                .build();

        sseService.sendOrderNotification(
                event.getUserId(),
                notification
        );

        log.info("주문 처리 및 SSE 알림 완료: {}", event.getOrderNumber());
    }
}
