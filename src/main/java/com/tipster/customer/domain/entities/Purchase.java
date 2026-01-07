package com.tipster.customer.domain.entities;

import com.tipster.customer.domain.enums.PurchaseStatusType;
import com.tipster.customer.domain.enums.TipStatusType;
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
@Table(name = "purchases", indexes = {
    @Index(name = "idx_purchases_tip_id", columnList = "tip_id"),
    @Index(name = "idx_purchases_buyer_id", columnList = "buyer_id"),
    @Index(name = "idx_purchases_status", columnList = "status"),
    @Index(name = "idx_purchases_purchased_at", columnList = "purchased_at"),
    @Index(name = "idx_purchases_tip_outcome", columnList = "tip_outcome")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_purchases_tip_buyer", columnNames = {"tip_id", "buyer_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_purchases_tip"))
    private Tip tip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_purchases_buyer"))
    private User buyer;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "purchase_status_type")
    private PurchaseStatusType status = PurchaseStatusType.PENDING;

    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_outcome", columnDefinition = "tip_status_type")
    private TipStatusType tipOutcome;

    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @Column(name = "purchased_at", nullable = false)
    private OffsetDateTime purchasedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        if (purchasedAt == null) {
            purchasedAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
