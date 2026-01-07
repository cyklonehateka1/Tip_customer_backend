package com.tipster.customer.domain.entities;

import com.tipster.customer.domain.enums.ReportStatusType;
import com.tipster.customer.domain.enums.ReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", indexes = {
    @Index(name = "idx_reports_reported_by", columnList = "reported_by"),
    @Index(name = "idx_reports_status", columnList = "status"),
    @Index(name = "idx_reports_type", columnList = "type"),
    @Index(name = "idx_reports_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false, foreignKey = @ForeignKey(name = "fk_reports_reported_by"))
    private User reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "report_type")
    private ReportType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "report_status_type")
    private ReportStatusType status = ReportStatusType.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", foreignKey = @ForeignKey(name = "fk_reports_reported_user"))
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_tip_id", foreignKey = @ForeignKey(name = "fk_reports_reported_tip"))
    private Tip reportedTip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_purchase_id", foreignKey = @ForeignKey(name = "fk_reports_reported_purchase"))
    private Purchase reportedPurchase;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by", foreignKey = @ForeignKey(name = "fk_reports_resolved_by"))
    private User resolvedBy;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

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
