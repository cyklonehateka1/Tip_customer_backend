package com.tipster.customer.domain.entities;

import com.tipster.customer.domain.enums.PredictionType;
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
@Table(name = "tip_selections", indexes = {
    @Index(name = "idx_tip_selections_tip_id", columnList = "tip_id"),
    @Index(name = "idx_tip_selections_match_id", columnList = "match_id"),
    @Index(name = "idx_tip_selections_prediction_type", columnList = "prediction_type")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_tip_selections", columnNames = {"tip_id", "match_id", "prediction_type", "prediction_value"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipSelection {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tip_selections_tip"))
    private Tip tip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tip_selections_match"))
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(name = "prediction_type", nullable = false, columnDefinition = "prediction_type")
    private PredictionType predictionType;

    @Column(name = "prediction_value", nullable = false, length = 100)
    private String predictionValue;

    @Column(name = "odds", precision = 8, scale = 2)
    private BigDecimal odds;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "is_void", nullable = false)
    private Boolean isVoid = false;

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
