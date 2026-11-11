package com.example.tickdeal.domain.notification.service;

import com.example.tickdeal.domain.notification.dto.OrderNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.tickdeal.common.constant.Constants.SSE_TIMEOUT;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final Map<Long, SseEmitter> emitters =
            new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        SseEmitter previous = emitters.put(userId, emitter);

        if (previous != null) {
            previous.complete();
        }

        emitter.onCompletion(() -> {
            emitters.remove(userId, emitter);
            log.info("SSE 연결 종료: userId={}", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId, emitter);
            emitter.complete();
            log.info("SSE 타임아웃: userId={}", userId);
        });

        emitter.onError(error -> {
            emitters.remove(userId, emitter);
            log.warn("SSE 연결 오류: userId={}", userId, error);
        });

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connect")
                            .data("SSE 연결성공")
            );
        } catch (IOException e) {
            emitters.remove(userId, emitter);
            emitter.completeWithError(e);
        }

        log.info("SSE 구독 등록: userId={}", userId);

        return emitter;
    }

    public void sendOrderNotification(
            Long userId,
            OrderNotificationDto notification
    ) {

        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.info("SSE 구독자가 없어 알림 생략: userId={}", userId);
            return;
        }

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("order")
                            .data(notification)
            );

            log.info(
                    "주문 SSE 알림 전송: userId={}, orderNumber={}, status={}",
                    userId,
                    notification.getOrderNumber(),
                    notification.getStatus()
            );
        } catch (IOException | IllegalStateException e) {

            emitters.remove(userId, emitter);
            emitter.completeWithError(e);
            log.warn("주문 SSE 알림 전송 실패: userId={}", userId, e);
        }
    }
}
