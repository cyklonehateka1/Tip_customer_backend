package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.MatchData;
import com.tipster.customer.domain.enums.MatchStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MatchData entity
 */
@Repository
public interface MatchDataRepository extends JpaRepository<MatchData, UUID> {
    
    /**
     * Find a match by its external ID (from The Odds API)
     * 
     * @param externalId The external ID from The Odds API
     * @return Optional MatchData if found
     */
    Optional<MatchData> findByExternalId(String externalId);
    
    /**
     * Check if a match exists by external ID
     * 
     * @param externalId The external ID from The Odds API
     * @return true if match exists, false otherwise
     */
    boolean existsByExternalId(String externalId);
    
    /**
     * Find upcoming matches from match_datetime onwards with specified status
     * 
     * @param fromDate The minimum match datetime
     * @param status The match status to filter by
     * @return List of upcoming matches
     */
    @Query("SELECT m FROM MatchData m " +
           "JOIN FETCH m.league l " +
           "JOIN FETCH m.homeTeam " +
           "JOIN FETCH m.awayTeam " +
           "WHERE m.matchDatetime >= :fromDate " +
           "AND m.status = :status " +
           "ORDER BY m.matchDatetime ASC")
    List<MatchData> findUpcomingMatches(
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("status") MatchStatusType status
    );
    
    /**
     * Find upcoming matches by league ID
     * 
     * @param leagueId The league ID
     * @param fromDate The minimum match datetime
     * @param status The match status to filter by
     * @return List of upcoming matches for the league
     */
    @Query("SELECT m FROM MatchData m " +
           "JOIN FETCH m.league l " +
           "JOIN FETCH m.homeTeam " +
           "JOIN FETCH m.awayTeam " +
           "WHERE m.league.id = :leagueId " +
           "AND m.matchDatetime >= :fromDate " +
           "AND m.status = :status " +
           "ORDER BY m.matchDatetime ASC")
    List<MatchData> findUpcomingMatchesByLeague(
        @Param("leagueId") UUID leagueId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("status") MatchStatusType status
    );
    
    /**
     * Find upcoming matches by league external ID
     * 
     * @param leagueExternalId The league external ID
     * @param fromDate The minimum match datetime
     * @param status The match status to filter by
     * @return List of upcoming matches for the league
     */
    @Query("SELECT m FROM MatchData m " +
           "JOIN FETCH m.league l " +
           "JOIN FETCH m.homeTeam " +
           "JOIN FETCH m.awayTeam " +
           "WHERE m.league.externalId = :leagueExternalId " +
           "AND m.matchDatetime >= :fromDate " +
           "AND m.status = :status " +
           "ORDER BY m.matchDatetime ASC")
    List<MatchData> findUpcomingMatchesByLeagueExternalId(
        @Param("leagueExternalId") String leagueExternalId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("status") MatchStatusType status
    );
}
