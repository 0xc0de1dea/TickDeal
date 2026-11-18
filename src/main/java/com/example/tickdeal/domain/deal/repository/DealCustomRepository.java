package com.example.tickdeal.domain.deal.repository;

import com.example.tickdeal.domain.deal.dto.DealDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DealCustomRepository {

    Page<DealDto.Response> searchDealByMultiCondition(String name, Pageable pageable);
}
