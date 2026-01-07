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
@Table(name = "personal_predictions", indexes = {
    @Index(name = "idx_personal_predictions_user_id", columnList = "user_id"),
    @Index(name = "idx_personal_predictions_status", columnList = "status"),
    @Index(name = "idx_personal_predictions_earliest_match_date", columnList = "earliest_match_date"),
    @Index(name = "idx_personal_predictions_user_status", columnList = "user_id,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalPrediction {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_personal_predictions_user"))
    private User user;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "total_stake", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalStake;

    @Column(name = "total_odds", precision = 8, scale = 2)
    private BigDecimal totalOdds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "tip_status_type")
    private TipStatusType status = TipStatusType.PENDING;

    @Column(name = "earliest_match_date", nullable = false)
    private OffsetDateTime earliestMatchDate;

    @Column(name = "created_at_12hr_before_match", nullable = false)
    private Boolean createdAt12hrBeforeMatch = false;

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
