package com.example.tickdeal.domain.product.repository;

import com.example.tickdeal.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    List<Product> findAllActive();

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isActive = true")
    Optional<Product> findActiveById(Long id);
}
