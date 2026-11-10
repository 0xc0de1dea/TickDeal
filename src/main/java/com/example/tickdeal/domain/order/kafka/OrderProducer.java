package com.example.tickdeal.domain.order.kafka;

import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.domain.order.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final String TOPIC = "tickdeal-order";

    public void sendOrder(OrderEvent event) {

        try {
            kafkaTemplate.send(
                    TOPIC,
                    event.getOrderNumber(),
                    event
            ).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceException(
                    ErrorCode.KAFKA_SEND_ORDER_FAILED
            );
        }
    }
}
