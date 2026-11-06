package com.example.tickdeal.domain.deal.dto;

import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.enums.DealStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DealDto {

    @Getter
    @AllArgsConstructor
    public static class Request {

        private Long productId;

        private Long dealPrice;

        private Integer stock;

        private LocalDateTime startAt;

        private LocalDateTime endAt;
    }

    @Getter
    @AllArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"productId", "productName", "originalPrice", "dealPrice", "stock"
                        , "startAt", "endAt", "status", "createdAt", "modifiedAt"})
    public static class Response {

        private Long productId;

        private String productName;

        private Long originalPrice;

        private Long dealPrice;

        private Integer stock;

        private LocalDateTime startAt;

        private LocalDateTime endAt;

        private DealStatus status;

        private LocalDateTime createdAt;

        private LocalDateTime modifiedAt;

        public static Response build(
                Long productId,
                String productName,
                Long originalPrice,
                Long dealPrice,
                Integer stock,
                LocalDateTime startAt,
                LocalDateTime endAt,
                DealStatus status,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt
        ) {
            return Response.builder()
                    .productId(productId)
                    .productName(productName)
                    .originalPrice(originalPrice)
                    .dealPrice(dealPrice)
                    .stock(stock)
                    .startAt(startAt)
                    .endAt(endAt)
                    .status(status)
                    .createdAt(createdAt)
                    .modifiedAt(modifiedAt)
                    .build();
        }

        public static Response from(Deal deal) {
            return Response.builder()
                    .productId(deal.getProduct().getId())
                    .productName(deal.getProduct().getName())
                    .originalPrice(
                            deal.getProduct().getPrice().longValue()
                    )
                    .dealPrice(deal.getDealPrice())
                    .stock(deal.getStock())
                    .startAt(deal.getStartAt())
                    .endAt(deal.getEndAt())
                    .status(deal.getStatus())
                    .createdAt(deal.getCreatedAt())
                    .modifiedAt(deal.getModifiedAt())
                    .build();
        }
    }
}
