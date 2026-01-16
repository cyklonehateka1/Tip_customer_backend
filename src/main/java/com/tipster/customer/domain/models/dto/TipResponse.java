package com.tipster.customer.domain.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipResponse {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal totalOdds;
    private String status;
    private Integer purchasesCount;
    private OffsetDateTime publishedAt;
    private OffsetDateTime earliestMatchDate;
    private OffsetDateTime createdAt;
    
    private TipsterBasicInfo tipster;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipsterBasicInfo {
        private UUID id;
        private String displayName;
        private String avatarUrl;
        private Boolean isVerified;
        private BigDecimal rating;
        private BigDecimal successRate;
        private Integer totalTips;
    }
}
