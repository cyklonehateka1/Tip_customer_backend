package com.tipster.customer.domain.entities;

import com.tipster.customer.infrastructure.utils.JsonConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tipster_applications", indexes = {
    @Index(name = "idx_tipster_applications_user_id", columnList = "user_id"),
    @Index(name = "idx_tipster_applications_status", columnList = "status"),
    @Index(name = "idx_tipster_applications_submitted_at", columnList = "submitted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipsterApplication {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tipster_applications_user"))
    private User user;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "identity_document_url", columnDefinition = "TEXT")
    private String identityDocumentUrl;

    @Column(name = "identity_document_type", length = 50)
    private String identityDocumentType;

    @Column(name = "proof_of_address_url", columnDefinition = "TEXT")
    private String proofOfAddressUrl;

    @Column(name = "payout_method", length = 50, nullable = false)
    private String payoutMethod;

    @Column(name = "payout_details", columnDefinition = "JSONB", nullable = false)
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> payoutDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", foreignKey = @ForeignKey(name = "fk_tipster_applications_reviewed_by"))
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (submittedAt == null) {
            submittedAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
