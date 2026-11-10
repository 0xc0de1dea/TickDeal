package com.example.tickdeal.domain.order.kafka;

import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.repository.DealRepository;
import com.example.tickdeal.domain.order.dto.OrderEvent;
import com.example.tickdeal.domain.order.entity.Order;
import com.example.tickdeal.domain.order.repository.OrderRepository;
import com.example.tickdeal.domain.order.service.OrderPersistenceService;
import com.example.tickdeal.domain.user.entity.User;
import com.example.tickdeal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderPersistenceService orderPersistenceService;

    @KafkaListener(
            topics = "tickdeal-order",
            groupId = "tickdeal-order-group"
    )
    @Transactional
    public void consume(OrderEvent event) {
        orderPersistenceService.saveOrder(event);
    }
}
