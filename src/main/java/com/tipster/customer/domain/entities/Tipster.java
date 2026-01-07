package com.tipster.customer.domain.entities;

import com.tipster.customer.infrastructure.utils.JsonConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tipsters", indexes = {
    @Index(name = "idx_tipsters_user_id", columnList = "user_id"),
    @Index(name = "idx_tipsters_is_ai", columnList = "is_ai"),
    @Index(name = "idx_tipsters_is_verified", columnList = "is_verified"),
    @Index(name = "idx_tipsters_kyc_status", columnList = "kyc_status"),
    @Index(name = "idx_tipsters_rating", columnList = "rating"),
    @Index(name = "idx_tipsters_success_rate", columnList = "success_rate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tipster {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_tipsters_user"))
    private User user;

    @Column(name = "is_ai", nullable = false)
    private Boolean isAi = false;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "total_tips", nullable = false)
    private Integer totalTips = 0;

    @Column(name = "successful_tips", nullable = false)
    private Integer successfulTips = 0;

    @Column(name = "total_earnings", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "success_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal successRate = BigDecimal.ZERO;

    @Column(name = "rating", precision = 5, scale = 2, nullable = false)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "kyc_status", length = 20)
    private String kycStatus = "not_applied";

    @Column(name = "kyc_submitted_at")
    private OffsetDateTime kycSubmittedAt;

    @Column(name = "kyc_approved_at")
    private OffsetDateTime kycApprovedAt;

    @Column(name = "kyc_rejected_at")
    private OffsetDateTime kycRejectedAt;

    @Column(name = "kyc_rejection_reason", columnDefinition = "TEXT")
    private String kycRejectionReason;

    @Column(name = "payout_method", length = 50)
    private String payoutMethod;

    @Column(name = "payout_details", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> payoutDetails;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
