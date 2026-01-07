package com.tipster.customer.domain.entities;

import com.tipster.customer.domain.enums.EscrowStatusType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "escrow", indexes = {
    @Index(name = "idx_escrow_purchase_id", columnList = "purchase_id"),
    @Index(name = "idx_escrow_status", columnList = "status"),
    @Index(name = "idx_escrow_is_ai_tip", columnList = "is_ai_tip"),
    @Index(name = "idx_escrow_held_at", columnList = "held_at"),
    @Index(name = "idx_escrow_released_at", columnList = "released_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_escrow_purchase", columnNames = "purchase_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Escrow {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_escrow_purchase"))
    private Purchase purchase;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "escrow_status_type")
    private EscrowStatusType status = EscrowStatusType.PENDING;

    @Column(name = "is_ai_tip", nullable = false)
    private Boolean isAiTip = false;

    @Column(name = "held_at")
    private OffsetDateTime heldAt;

    @Column(name = "released_at")
    private OffsetDateTime releasedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "released_to", foreignKey = @ForeignKey(name = "fk_escrow_released_to"))
    private User releasedTo;

    @Column(name = "release_type", length = 20)
    private String releaseType;

    @Column(name = "platform_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "platform_fee_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal platformFeePercentage = BigDecimal.ZERO;

    @Column(name = "tipster_earnings", precision = 10, scale = 2, nullable = false)
    private BigDecimal tipsterEarnings = BigDecimal.ZERO;

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
