package com.example.tickdeal.domain.order.repository;

import com.example.tickdeal.domain.order.entity.Order;
import com.example.tickdeal.domain.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    Optional<Order> findByOrderNumberAndUserId(
            String orderNumber,
            Long userId
    );

    Page<Order> findAllByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Page<Order> findAllByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            OrderStatus status,
            Pageable pageable
    );
}
