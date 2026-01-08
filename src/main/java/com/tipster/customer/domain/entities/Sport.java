package com.tipster.customer.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sports", indexes = {
    @Index(name = "idx_sports_group", columnList = "sport_group"),
    @Index(name = "idx_sports_key", columnList = "sport_key"),
    @Index(name = "idx_sports_is_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_sports_key", columnNames = "sport_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sport {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sport_key", nullable = false, unique = true, length = 100)
    private String sportKey; // e.g., "soccer_epl", "basketball_nba"

    @Column(name = "title", nullable = false, length = 255)
    private String title; // e.g., "EPL", "NBA"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // e.g., "English Premier League"

    @Column(name = "sport_group", nullable = false, length = 100)
    private String sportGroup; // e.g., "Soccer", "Basketball", "American Football"

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "has_outrights", nullable = false)
    private Boolean hasOutrights = false;

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
