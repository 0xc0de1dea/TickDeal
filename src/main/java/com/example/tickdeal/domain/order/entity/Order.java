package com.example.tickdeal.domain.order.entity;

import com.example.tickdeal.common.entity.BaseEntity;
import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.order.enums.OrderStatus;
import com.example.tickdeal.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orders_order_number",
                        columnNames = "order_number"
                )
        }
)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "order_number",
            nullable = false,
            unique = true,
            length = 36
    )
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    public Order(
            String orderNumber,
            User user,
            Deal deal,
            Integer quantity,
            Long orderPrice
    ) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.deal = deal;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.status = OrderStatus.PENDING;
    }

    public static String generateOrderNumber() {
        return UUID.randomUUID().toString();
    }

    public void complete() {
        if (this.status != OrderStatus.PENDING) {
            throw new ServiceException(
                    ErrorCode.ORDER_CAN_COMPLETE_ON_PENDING
            );
        }

        this.status = OrderStatus.COMPLETED;
    }

    public void fail() {
        if (this.status != OrderStatus.PENDING) {
            throw new ServiceException(
                    ErrorCode.ORDER_CAN_FAIL_PROCESS_ON_PENDING
            );
        }

        this.status = OrderStatus.FAILED;
    }
}
