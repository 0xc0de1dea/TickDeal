package com.example.tickdeal.domain.deal.repository;

import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.enums.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findAllByStatus(DealStatus status);
}
