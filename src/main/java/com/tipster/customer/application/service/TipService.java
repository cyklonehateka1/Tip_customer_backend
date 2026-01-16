package com.tipster.customer.application.service;

import com.tipster.customer.domain.models.dto.TipResponse;
import com.tipster.customer.domain.models.dto.TipsPageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface TipService {
    TipsPageResponse getTips(
            String keyword,
            UUID tipsterId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String status,
            Boolean isFree,
            Pageable pageable
    );
}
