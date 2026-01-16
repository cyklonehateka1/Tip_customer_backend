package com.tipster.customer.application.service.impl;

import com.tipster.customer.application.service.TipService;
import com.tipster.customer.domain.entities.Tip;
import com.tipster.customer.domain.enums.TipStatusType;
import com.tipster.customer.domain.models.dto.TipResponse;
import com.tipster.customer.domain.models.dto.TipsPageResponse;
import com.tipster.customer.domain.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TipServiceImpl implements TipService {

    private final TipRepository tipRepository;

    @Override
    public TipsPageResponse getTips(
            String keyword,
            UUID tipsterId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String status,
            Boolean isFree,
            Pageable pageable
    ) {

        TipStatusType statusEnum = null;
        if (status != null) {
            try {
                statusEnum = TipStatusType.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status");
            }
        }

        // handle isFree WITHOUT SQL bullshit
        BigDecimal effectiveMin = minPrice;
        BigDecimal effectiveMax = maxPrice;

        if (isFree != null) {
            if (isFree) {
                effectiveMax = BigDecimal.ZERO;
            } else {
                effectiveMin = BigDecimal.ZERO;
            }
        }

        Page<Tip> page = tipRepository.findPublishedTipsWithFilters(
                keyword,
                tipsterId,
                effectiveMin,
                effectiveMax,
                statusEnum,
                pageable
        );

        TipsPageResponse response = new TipsPageResponse();
        response.setTips(
                page.getContent().stream()
                        .map(this::mapToResponse)
                        .toList()
        );
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setFreeTipsCount(tipRepository.countFreeTips());
        response.setAvailableTipsCount(
                tipRepository.countAvailableTips(
                        OffsetDateTime.now(),
                        com.tipster.customer.domain.enums.MatchStatusType.scheduled
                )
        );

    return response;
    }

    private TipResponse mapToResponse(Tip tip) {
        TipResponse response = new TipResponse();
        response.setId(tip.getId());
        response.setTitle(tip.getTitle());
        response.setDescription(tip.getDescription());
        response.setPrice(tip.getPrice());
        response.setTotalOdds(tip.getTotalOdds());
        response.setStatus(tip.getStatus() != null ? tip.getStatus().name() : null);
        response.setPurchasesCount(tip.getPurchasesCount() != null ? tip.getPurchasesCount() : 0);
        response.setPublishedAt(tip.getPublishedAt());
        response.setEarliestMatchDate(tip.getEarliestMatchDate());
        response.setCreatedAt(tip.getCreatedAt());

        // Map tipster info
        if (tip.getTipster() != null) {
            TipResponse.TipsterBasicInfo tipsterInfo = new TipResponse.TipsterBasicInfo();
            tipsterInfo.setId(tip.getTipster().getId());
            
            // Get display name from user
            if (tip.getTipster().getUser() != null) {
                tipsterInfo.setDisplayName(tip.getTipster().getUser().getDisplayName());
                tipsterInfo.setAvatarUrl(tip.getTipster().getUser().getAvatarUrl());
            } else {
                tipsterInfo.setDisplayName(tip.getTipster().getBio()); // Fallback to bio
                tipsterInfo.setAvatarUrl(tip.getTipster().getAvatarUrl());
            }
            
            tipsterInfo.setIsVerified(tip.getTipster().getIsVerified() != null ? tip.getTipster().getIsVerified() : false);
            tipsterInfo.setRating(tip.getTipster().getRating());
            tipsterInfo.setSuccessRate(tip.getTipster().getSuccessRate());
            tipsterInfo.setTotalTips(tip.getTipster().getTotalTips() != null ? tip.getTipster().getTotalTips() : 0);
            
            response.setTipster(tipsterInfo);
        }

        return response;
    }
}