package com.example.tickdeal.domain.order.service;

import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.enums.DealStatus;
import com.example.tickdeal.domain.deal.repository.DealRepository;
import com.example.tickdeal.domain.inventory.service.InventoryService;
import com.example.tickdeal.domain.order.dto.OrderDto;
import com.example.tickdeal.domain.order.dto.OrderEvent;
import com.example.tickdeal.domain.order.entity.Order;
import com.example.tickdeal.domain.order.enums.OrderStatus;
import com.example.tickdeal.domain.order.kafka.OrderProducer;
import com.example.tickdeal.domain.order.repository.OrderRepository;
import com.example.tickdeal.domain.user.entity.User;
import com.example.tickdeal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;

    private final DealRepository dealRepository;

    private final OrderRepository orderRepository;

    private final InventoryService inventoryService;

    private final OrderProducer orderProducer;

    public OrderDto.Response createOrder(
            org.springframework.security.core.userdetails.User user,
            OrderDto.Request request
    ) {
        String email = user.getUsername();

        User savedUser = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        Deal deal = dealRepository.findById(request.getDealId())
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.DEAL_NOT_FOUND)
                );

        LocalDateTime now = LocalDateTime.now();

        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new ServiceException(ErrorCode.ORDER_NOT_ACTIVE);
        }

        if (now.isBefore(deal.getStartAt())
        || !now.isBefore(deal.getEndAt())) {
            throw new ServiceException(ErrorCode.ORDER_NOT_PURCHASE_TIME);
        }

        Integer quantity = request.getQuantity();

        if (quantity == null || quantity <= 0) {
            throw new ServiceException(ErrorCode.ORDER_QUANTITY_LESS_THAN_ZERO);
        }

        String orderNumber = Order.generateOrderNumber();

        Long orderPrice = deal.getDealPrice() * quantity;

        Long remainingStock = inventoryService.decreaseStock(
                deal.getId(),
                quantity
        );

        if (remainingStock == -1) {
            throw new ServiceException(ErrorCode.STOCK_INFORMATION_NOT_EXISTS_REDIS);
        }

        if (remainingStock == -2) {
            throw new ServiceException(ErrorCode.ORDER_INVALID);
        }

        if (remainingStock == 0) {
            throw new ServiceException(ErrorCode.STOCK_LACK_REDIS);
        }

        OrderEvent event = OrderEvent.builder()
                .orderNumber(orderNumber)
                .userId(savedUser.getId())
                .dealId(deal.getId())
                .quantity(quantity)
                .orderPrice(orderPrice)
                .createdAt(now)
                .build();

        try {
            orderProducer.sendOrder(event);
        } catch (Exception e) {
            inventoryService.restoreStock(
                    deal.getId(),
                    quantity
            );

            throw new ServiceException(
                    ErrorCode.ORDER_EVENT_PRODUCE_FAILED
            );
        }

        return OrderDto.Response.build(
                orderNumber,
                OrderStatus.PENDING,
                orderPrice,
                quantity,
                now
        );
    }

    @Transactional(readOnly = true)
    public OrderDto.DetailResponse getOrder(
            org.springframework.security.core.userdetails.User user,
            String orderNumber
    ) {
        String email = user.getUsername();

        User savedUser = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        Order order = orderRepository
                .findByOrderNumberAndUserId(orderNumber, savedUser.getId())
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.ORDER_NOT_FOUND)
                );

        return OrderDto.DetailResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto.DetailResponse> getOrders(
            org.springframework.security.core.userdetails.User user,
            int page,
            int size
    ) {
        String email = user.getUsername();

        User savedUser = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        Pageable pageable = PageRequest.of(
                page,
                size
        );

        return orderRepository
                .findAllByUserIdOrderByCreatedAtDesc(savedUser.getId(), pageable)
                .map(OrderDto.DetailResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto.DetailResponse> getPendingOrders(
            org.springframework.security.core.userdetails.User user,
            int page,
            int size
    ) {
        String email = user.getUsername();

        User savedUser = userRepository.findUserByEmail(email)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
                );

        Pageable pageable = PageRequest.of(
                page,
                size
        );

        return orderRepository
                .findAllByUserIdAndStatusOrderByCreatedAtDesc(
                        savedUser.getId(),
                        OrderStatus.PENDING,
                        pageable
                )
                .map(OrderDto.DetailResponse::from);
    }
}
