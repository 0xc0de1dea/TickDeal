package com.example.tickdeal.domain.deal.entity;

import com.example.tickdeal.common.entity.BaseEntity;
import com.example.tickdeal.domain.deal.enums.DealStatus;
import com.example.tickdeal.domain.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "deals"
)
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long dealPrice;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealStatus status;

    public Deal(
            Product product,
            Long dealPrice,
            Integer stock,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        this.product = product;
        this.dealPrice = dealPrice;
        this.stock = stock;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = DealStatus.SCHEDULED;
    }

    // 타임딜 수정
    public void update(
            Long dealPrice,
            Integer stock,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        this.dealPrice = dealPrice == null ? this.dealPrice : dealPrice;
        this.stock = stock == null ? this.stock : stock;
        this.startAt = startAt == null ? this.startAt : startAt;
        this.endAt = endAt == null ? this.endAt : endAt;
    }

    // 타임딜 시작
    public void activate() {
        this.status = DealStatus.ACTIVE;
    }

    // 타임딜 종료
    public void end() {
        this.status = DealStatus.ENDED;
    }

    // 재고 소진
    public void soldOut() {
        this.status = DealStatus.SOLD_OUT;
    }
}
