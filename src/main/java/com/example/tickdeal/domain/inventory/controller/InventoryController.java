package com.example.tickdeal.domain.inventory.controller;

import com.example.tickdeal.common.dto.ApiResponse;
import com.example.tickdeal.domain.inventory.dto.InventoryDto;
import com.example.tickdeal.domain.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{dealId}")
    public ResponseEntity<ApiResponse<InventoryDto.Response>> getStock(
            @PathVariable Long dealId
    ) {
        Long stock = inventoryService.getStock(dealId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(InventoryDto.Response.build(dealId, stock)));
    }
}
