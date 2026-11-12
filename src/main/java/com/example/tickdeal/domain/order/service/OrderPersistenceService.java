package com.example.tickdeal.domain.order.service;

import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.repository.DealRepository;
import com.example.tickdeal.domain.order.dto.OrderEvent;
import com.example.tickdeal.domain.order.entity.Order;
import com.example.tickdeal.domain.order.enums.OrderStatus;
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

//    @Transactional
//    public void saveOrder(OrderEvent event) {
//
//        if (orderRepository.existsByOrderNumber(event.getOrderNumber())) {
//            return;
//        }
//
//        User user = userRepository.findById(event.getUserId())
//                .orElseThrow(
//                        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
//                );
//
//        Deal deal = dealRepository.findById(event.getDealId())
//                .orElseThrow(
//                        () -> new ServiceException(ErrorCode.DEAL_NOT_FOUND)
//                );
//
//        int updated = dealRepository.decreaseStock(
//                event.getDealId(),
//                event.getQuantity()
//        );
//
//        if (updated == 0) {
//            throw new ServiceException(ErrorCode.STOCK_LACK_MYSQL);
//        }
//
//        Order order = new Order(
//                event.getOrderNumber(),
//                user,
//                deal,
//                event.getQuantity(),
//                event.getOrderPrice()
//        );
//
//        order.complete();
//
//        orderRepository.save(order);
//    }

    /**
     * 주문 접수 시 PENDING 주문 생성
     */
    @Transactional
    public void createPendingOrder(OrderEvent event) {

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

        Order order = new Order(
                event.getOrderNumber(),
                user,
                deal,
                event.getQuantity(),
                event.getOrderPrice()
        );

        orderRepository.save(order);
    }

    /**
     * Kafka Consumer 주문 완료 처리
     *
     * PENDING -> COMPLETED
     * MySQL 재고 차감과 주문 상태 변경을 하나의 트랜잭션으로 처리
     */
    @Transactional
    public boolean completeOrder(OrderEvent event) {

        Order order = orderRepository
                .findByOrderNumberForUpdate(event.getOrderNumber())
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.ORDER_NOT_FOUND)
                );

        // 중복 Kafka 이벤트
        if (order.getStatus() == OrderStatus.COMPLETED) {
            return false;
        }

        // 실패 주문은 완료할 수 없음
        if (order.getStatus() == OrderStatus.FAILED) {
            return false;
        }

        int updated = dealRepository.decreaseStock(
                order.getDeal().getId(),
                order.getQuantity()
        );

        if (updated == 0) {
            throw new ServiceException(ErrorCode.STOCK_LACK_MYSQL);
        }

        order.complete();

        return true;
    }

    /**
     * 주문 실패 처리
     *
     * PENDING -> FAILED
     */
    @Transactional
    public boolean failOrder(String orderNumber) {

        Order order = orderRepository
                .findByOrderNumberForUpdate(orderNumber)
                .orElse(null);

        // 이미 완료된 주문은 실패 처리하지 않음
        if (order.getStatus() == OrderStatus.COMPLETED) {
            return false;
        }

        // 이미 실패했으면 멱등 처리
        if (order.getStatus() == OrderStatus.FAILED) {
            return false;
        }

        order.fail();

        return true;
    }

    /**
     * 주문 상태 조회
     */
    @Transactional(readOnly = true)
    public OrderStatus getOrderStatus(String orderNumber) {

        return orderRepository
                .findByOrderNumber(orderNumber)
                .map(Order::getStatus)
                .orElse(null);
    }
}
