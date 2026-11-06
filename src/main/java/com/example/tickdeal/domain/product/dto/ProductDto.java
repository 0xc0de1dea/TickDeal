package com.example.tickdeal.domain.product.dto;

import com.example.tickdeal.domain.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class ProductDto {

    @Getter
    @AllArgsConstructor
    public static class Request {

        private String name;

        private String description;

        private Long price;

        private String imageUrl;
    }

    @Getter
    @AllArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"name", "description", "price", "imageUrl", "createdAt", "modifiedAt"})
    public static class Response {

        private String name;

        private String description;

        private Long price;

        private String imageUrl;

        private LocalDateTime createdAt;

        private LocalDateTime modifiedAt;

        public static Response build(
                String name,
                String description,
                Long price,
                String imageUrl,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt
        ) {
            return Response.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .imageUrl(imageUrl)
                    .createdAt(createdAt)
                    .modifiedAt(modifiedAt)
                    .build();
        }

        public static Response from(Product product) {
            return new Response(
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getImageUrl(),
                    product.getCreatedAt(),
                    product.getModifiedAt()
            );
        }
    }
}
