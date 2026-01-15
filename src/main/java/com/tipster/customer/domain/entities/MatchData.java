package com.tipster.customer.domain.entities;

import com.tipster.customer.domain.enums.MatchStatusType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity for the match_data table
 * Stores match information from The Odds API
 */
@Entity
@Table(name = "match_data", indexes = {
    @Index(name = "idx_match_data_external_id", columnList = "external_id"),
    @Index(name = "idx_match_data_league_id", columnList = "league_id"),
    @Index(name = "idx_match_data_home_team_id", columnList = "home_team_id"),
    @Index(name = "idx_match_data_away_team_id", columnList = "away_team_id"),
    @Index(name = "idx_match_data_match_datetime", columnList = "match_datetime"),
    @Index(name = "idx_match_data_status", columnList = "status"),
    @Index(name = "idx_match_data_datetime_status", columnList = "match_datetime,status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_match_data_external_id", columnNames = "external_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchData {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_id", length = 100, unique = true, nullable = false)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", foreignKey = @ForeignKey(name = "fk_match_data_league"))
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_data_home_team"))
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_data_away_team"))
    private Team awayTeam;

    @Column(name = "match_datetime", nullable = false)
    private OffsetDateTime matchDatetime;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "match_status_type")
    private MatchStatusType status = MatchStatusType.scheduled;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "home_score_penalty")
    private Integer homeScorePenalty;

    @Column(name = "away_score_penalty")
    private Integer awayScorePenalty;

    @Column(name = "venue", length = 255)
    private String venue;

    @Column(name = "referee", length = 255)
    private String referee;

    @Column(name = "round", length = 100)
    private String round;

    @Column(name = "season", length = 50)
    private String season;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @Column(name = "odds", columnDefinition = "TEXT")
    private String odds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
