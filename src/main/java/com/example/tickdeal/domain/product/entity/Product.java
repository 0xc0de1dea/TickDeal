package com.example.tickdeal.domain.product.entity;

import com.example.tickdeal.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "products"
)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "상품 이름은 필수입니다.")
    @Size(
            max = 50,
            message = "상품 이름은 최대 50글자까지 입력할 수 있습니다."
    )
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "상품 설명은 필수입니다.")
    @Size(
            max = 100,
            message = "상품 설명은 최대 100글자까지 입력할 수 있습니다."
    )
    @Column(nullable = false)
    private String description;

    @NotNull(message = "상품 가격은 필수입니다.")
    @PositiveOrZero(message = "상품 가격은 0 이상이어야 합니다.")
    @Column(nullable = false)
    private Long price;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    // 주문 데이터 보존을 위해 soft delete를 고려
    private boolean isActive;

    public Product(
            String name,
            String description,
            Long price,
            String imageUrl
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.isActive = true;
    }

    public void update(
            String name,
            String description,
            Long price,
            String imageUrl
    ) {
        this.name = name == null ? this.name : name;
        this.description = description == null ? this.description : description;
        this.price = price == null ? this.price : price;
        this.imageUrl = imageUrl == null ? this.imageUrl : imageUrl;
    }

    public void deActive() {
        this.isActive = false;
    }
}
