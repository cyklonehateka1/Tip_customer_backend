package com.tipster.customer.domain.entities;

import com.tipster.customer.domain.enums.TransactionStatusType;
import com.tipster.customer.domain.enums.TransactionType;
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
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transactions_user_id", columnList = "user_id"),
    @Index(name = "idx_transactions_tipster_id", columnList = "tipster_id"),
    @Index(name = "idx_transactions_type", columnList = "type"),
    @Index(name = "idx_transactions_status", columnList = "status"),
    @Index(name = "idx_transactions_purchase_id", columnList = "purchase_id"),
    @Index(name = "idx_transactions_created_at", columnList = "created_at"),
    @Index(name = "idx_transactions_user_type", columnList = "user_id,type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_transactions_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipster_id", foreignKey = @ForeignKey(name = "fk_transactions_tipster"))
    private Tipster tipster;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "transaction_type")
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "transaction_status_type")
    private TransactionStatusType status = TransactionStatusType.PENDING;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "GHS";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", foreignKey = @ForeignKey(name = "fk_transactions_purchase"))
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escrow_id", foreignKey = @ForeignKey(name = "fk_transactions_escrow"))
    private Escrow escrow;

    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "metadata", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> metadata;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

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
