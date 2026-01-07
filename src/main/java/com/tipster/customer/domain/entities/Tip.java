package com.tipster.customer.domain.entities;

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
@Table(name = "tips", indexes = {
    @Index(name = "idx_tips_tipster_id", columnList = "tipster_id"),
    @Index(name = "idx_tips_is_ai", columnList = "is_ai"),
    @Index(name = "idx_tips_status", columnList = "status"),
    @Index(name = "idx_tips_is_published", columnList = "is_published"),
    @Index(name = "idx_tips_published_at", columnList = "published_at"),
    @Index(name = "idx_tips_earliest_match_date", columnList = "earliest_match_date"),
    @Index(name = "idx_tips_status_published", columnList = "status,is_published"),
    @Index(name = "idx_tips_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tip {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipster_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tips_tipster"))
    private Tipster tipster;

    @Column(name = "is_ai", nullable = false)
    private Boolean isAi = false;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "total_odds", precision = 8, scale = 2)
    private BigDecimal totalOdds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "tip_status_type")
    private TipStatusType status = TipStatusType.PENDING;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "purchases_count", nullable = false)
    private Integer purchasesCount = 0;

    @Column(name = "total_revenue", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "earliest_match_date")
    private OffsetDateTime earliestMatchDate;

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
