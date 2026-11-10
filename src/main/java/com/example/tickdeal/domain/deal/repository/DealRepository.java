package com.example.tickdeal.domain.deal.repository;

import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.enums.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findAllByStatus(DealStatus status);

    @Modifying
    @Query("""
    UPDATE Deal d
    SET d.stock = d.stock - :quantity
    WHERE d.id = :dealId
      AND d.stock >= :quantity
""")
    int decreaseStock(
            @Param("dealId") Long dealId,
            @Param("quantity") Integer quantity
    );
}
