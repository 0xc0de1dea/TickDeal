package com.example.tickdeal.domain.order.kafka;

import com.example.tickdeal.domain.inventory.service.InventoryService;
import com.example.tickdeal.domain.notification.dto.OrderNotificationDto;
import com.example.tickdeal.domain.notification.service.SseService;
import com.example.tickdeal.domain.order.dto.OrderEvent;
import com.example.tickdeal.domain.order.enums.OrderStatus;
import com.example.tickdeal.domain.order.service.OrderPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDltConsumer {

    private final OrderPersistenceService orderPersistenceService;
    private final InventoryService inventoryService;
    private final SseService sseService;

    @KafkaListener(
            topics = "tickdeal-order.DLT",
            groupId = "tickdeal-order-dlt-group"
    )
    public void consumeDlt(OrderEvent event) {

        log.error(
                "주문 이벤트 최종 실패: {}",
                event.getOrderNumber()
        );

        // 1. 이미 완료된 주문인지 확인
        OrderStatus status =
                orderPersistenceService.getOrderStatus(
                        event.getOrderNumber()
                );

        // 완료된 주문은 Redis 복구 금지
        if (status == OrderStatus.COMPLETED) {
            log.warn(
                    "이미 완료된 주문이 DLT에 도착했습니다: {}",
                    event.getOrderNumber()
            );
            return;
        }

        // 2. PENDING -> FAILED
        boolean failed =
                orderPersistenceService.failOrder(
                        event.getOrderNumber()
                );

        // 3. Redis 재고 복구 (주문별 1회)
        boolean restored =
                inventoryService.restoreStockOnce(
                        event.getDealId(),
                        event.getOrderNumber(),
                        event.getQuantity()
                );

        log.info(
                "주문 실패 보상 결과: orderNumber={}, failed={}, restored={}",
                event.getOrderNumber(),
                failed,
                restored
        );

        // 4. 주문 실패 알림
        if (failed || restored) {

            OrderNotificationDto notification =
                    OrderNotificationDto.builder()
                            .orderNumber(event.getOrderNumber())
                            .status(OrderStatus.FAILED)
                            .message("주문 처리에 실패했습니다. 재고가 복구되었습니다.")
                            .createdAt(LocalDateTime.now())
                            .build();

            sseService.sendOrderNotification(
                    event.getUserId(),
                    notification
            );
        }
    }
}