package com.example.tickdeal.domain.product.controller;

import com.example.tickdeal.common.dto.ApiResponse;
import com.example.tickdeal.domain.product.dto.ProductDto;
import com.example.tickdeal.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto.Response>> create(
            @Valid @RequestBody ProductDto.Request request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productService.create(request)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDto.Response>> findOne(
            @PathVariable Long productId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(productService.findOne(productId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> findAll(

    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(productService.findAll()));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDto.Response>> update(
            @PathVariable Long productId,
            @Valid @RequestBody ProductDto.Request request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(productService.update(productId, request)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productId
    ) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent());
    }
}
