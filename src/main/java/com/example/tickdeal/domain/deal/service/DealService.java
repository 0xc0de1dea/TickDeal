package com.example.tickdeal.domain.deal.service;

import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import com.example.tickdeal.domain.deal.dto.DealDto;
import com.example.tickdeal.domain.deal.entity.Deal;
import com.example.tickdeal.domain.deal.enums.DealStatus;
import com.example.tickdeal.domain.deal.repository.DealRepository;
import com.example.tickdeal.domain.inventory.service.InventoryService;
import com.example.tickdeal.domain.product.entity.Product;
import com.example.tickdeal.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    // TEST용 ACTIVE만들기
    @Transactional
    public void active(Long dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.DEAL_NOT_FOUND)
                );

        deal.activate();
        deal.update(
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );
    }

    @Transactional
    public DealDto.Response create(
            DealDto.Request request
    ) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND)
                );

        if (!product.isActive()) {
            throw new ServiceException(ErrorCode.PRODUCT_IS_DE_ACTIVE);
        }

        Deal deal = new Deal(
                product,
                request.getDealPrice(),
                request.getStock(),
                request.getStartAt(),
                request.getEndAt()
        );

        Deal saved = dealRepository.saveAndFlush(deal);

        inventoryService.initializeStock(
                saved.getId(),
                saved.getStock()
        );

        return DealDto.Response.from(saved);
    }

    @Transactional(readOnly = true)
    public DealDto.Response findOne(Long dealId) {
        Deal saved = dealRepository.findById(dealId)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.DEAL_NOT_FOUND)
                );

        return DealDto.Response.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DealDto.Response> findAll() {

        return dealRepository.findAll()
                .stream()
                .map(DealDto.Response::from)
                .toList();
    }

    @Transactional
    public DealDto.Response update(
            Long dealId,
            DealDto.Request request
    ) {
        Deal saved = dealRepository.findById(dealId)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.DEAL_NOT_FOUND)
                );

        if (saved.getStatus() != DealStatus.SCHEDULED) {
            throw new ServiceException(ErrorCode.DEAL_ALREADY_IN_PROGRESS);
        }

        saved.update(
                request.getDealPrice(),
                request.getStock(),
                request.getStartAt(),
                request.getEndAt()
        );

        return DealDto.Response.build(
                saved.getProduct().getId(),
                saved.getProduct().getName(),
                saved.getProduct().getPrice(),
                saved.getDealPrice(),
                saved.getStock(),
                saved.getStartAt(),
                saved.getEndAt(),
                saved.getStatus(),
                saved.getCreatedAt(),
                LocalDateTime.now()
        );
    }

    @Transactional
    public void delete(Long dealId) {
        Deal saved = dealRepository.findById(dealId)
                .orElseThrow(
                        () -> new ServiceException(ErrorCode.DEAL_NOT_FOUND)
                );

        if (saved.getStatus() != DealStatus.SCHEDULED) {
            throw new ServiceException(ErrorCode.DEAL_ALREADY_IN_PROGRESS);
        }

        dealRepository.delete(saved);
    }
}
