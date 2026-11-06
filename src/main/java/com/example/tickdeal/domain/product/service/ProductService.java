package com.example.tickdeal.domain.product.service;

import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.domain.product.dto.ProductDto;
import com.example.tickdeal.domain.product.entity.Product;
import com.example.tickdeal.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDto.Response create(ProductDto.Request request) {
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getImageUrl()
        );

        Product saved = productRepository.saveAndFlush(product);

        return ProductDto.Response.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductDto.Response findOne(Long productId) {
        Product saved = productRepository.findActiveById(productId)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND)
                );

        return ProductDto.Response.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductDto.Response> findAll() {

        return productRepository.findAllActive()
                .stream()
                .map(ProductDto.Response::from)
                .toList();
    }

    @Transactional
    public ProductDto.Response update(
            Long productId,
            ProductDto.Request request
    ) {
        Product saved = productRepository.findActiveById(productId)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND)
                );

        saved.update(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getImageUrl()
        );

        return ProductDto.Response.build(
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getImageUrl(),
                saved.getCreatedAt(),
                LocalDateTime.now()
        );
    }

    @Transactional
    public void delete(Long productId) {
        Product saved = productRepository.findActiveById(productId)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND)
                );

        saved.deActive();
    }
}
