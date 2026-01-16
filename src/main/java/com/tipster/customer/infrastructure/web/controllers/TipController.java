package com.tipster.customer.infrastructure.web.controllers;

import com.tipster.customer.application.service.TipService;
import com.tipster.customer.domain.models.ApiResponse;
import com.tipster.customer.domain.models.dto.TipsPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/tips")
@RequiredArgsConstructor
public class TipController {

    private final TipService tipService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<ApiResponse<TipsPageResponse>> getTips(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID tipsterId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // Create pageable with default sorting by tipster rating/success rate and published date
        // The repository query already handles this ordering
        Pageable pageable = PageRequest.of(page, size);
        
        TipsPageResponse response = tipService.getTips(
                keyword,
                tipsterId,
                minPrice,
                maxPrice,
                status,
                isFree,
                pageable
        );
        
        return ResponseEntity.ok(ApiResponse.success("Tips retrieved successfully", response));
    }
}
