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

@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_matches_league_id", columnList = "league_id"),
    @Index(name = "idx_matches_home_team_id", columnList = "home_team_id"),
    @Index(name = "idx_matches_away_team_id", columnList = "away_team_id"),
    @Index(name = "idx_matches_match_date", columnList = "match_date"),
    @Index(name = "idx_matches_status", columnList = "status"),
    @Index(name = "idx_matches_external_id", columnList = "external_id"),
    @Index(name = "idx_matches_date_status", columnList = "match_date,status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_matches_external_id", columnNames = "external_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", foreignKey = @ForeignKey(name = "fk_matches_league"))
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matches_home_team"))
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matches_away_team"))
    private Team awayTeam;

    @Column(name = "match_date", nullable = false)
    private OffsetDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "match_status_type")
    private MatchStatusType status = MatchStatusType.SCHEDULED;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "odds_json", columnDefinition = "jsonb")
    private String oddsJson;

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
