package com.example.tickdeal.domain.deal.controller;

import com.example.tickdeal.common.dto.ApiResponse;
import com.example.tickdeal.domain.deal.dto.DealDto;
import com.example.tickdeal.domain.deal.service.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;

    // TEST용 active
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long id
    ) {
        dealService.active(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DealDto.Response>> create(
            @RequestBody DealDto.Request request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(dealService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DealDto.Response>> findOne(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(dealService.findOne(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DealDto.Response>>> findAll() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(dealService.findAll()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DealDto.Response>> update(
            @PathVariable Long id,
            @RequestBody DealDto.Request request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(
                dealService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {
        dealService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent());
    }
}
