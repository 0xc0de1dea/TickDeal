package com.example.tickdeal.domain.inventory.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class InventoryDto {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response {

        private Long dealId;

        private Long stock;

        private boolean soldOut;

        public static Response build(Long dealId, Long stock) {
            return Response.builder()
                    .dealId(dealId)
                    .stock(stock)
                    .soldOut(stock == 0)
                    .build();
        }
    }
}
