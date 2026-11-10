package com.example.tickdeal.domain.order.service;

import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.repository.DealRepository;
import com.example.tickdeal.domain.order.dto.OrderEvent;
import com.example.tickdeal.domain.order.entity.Order;
import com.example.tickdeal.domain.order.repository.OrderRepository;
import com.example.tickdeal.domain.user.entity.User;
import com.example.tickdeal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final DealRepository dealRepository;

    @Transactional
    public void saveOrder(OrderEvent event) {

        if (orderRepository.existsByOrderNumber(event.getOrderNumber())) {
            return;
        }

        User user = userRepository.findById(event.getUserId())
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        Deal deal = dealRepository.findById(event.getDealId())
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.DEAL_NOT_FOUND)
                );

        int updated = dealRepository.decreaseStock(
                event.getDealId(),
                event.getQuantity()
        );

        if (updated == 0) {
            throw new ServiceException(ErrorCode.STOCK_LACK_MYSQL);
        }

        Order order = new Order(
                event.getOrderNumber(),
                user,
                deal,
                event.getQuantity(),
                event.getOrderPrice()
        );

        order.complete();

        orderRepository.save(order);
    }
}
