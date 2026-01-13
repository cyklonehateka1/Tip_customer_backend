package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.League;
import com.tipster.customer.domain.entities.Match;
import com.tipster.customer.domain.enums.MatchStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    
    Optional<Match> findByExternalId(String externalId);
    
    @Query("SELECT m FROM Match m " +
           "JOIN FETCH m.league l " +
           "JOIN FETCH m.homeTeam " +
           "JOIN FETCH m.awayTeam " +
           "WHERE m.matchDate >= :fromDate " +
           "AND m.status = :status " +
           "ORDER BY m.matchDate ASC")
    List<Match> findUpcomingMatches(
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("status") MatchStatusType status
    );
    
    @Query("SELECT m FROM Match m " +
           "JOIN FETCH m.league l " +
           "JOIN FETCH m.homeTeam " +
           "JOIN FETCH m.awayTeam " +
           "WHERE m.league.id = :leagueId " +
           "AND m.matchDate >= :fromDate " +
           "AND m.status = :status " +
           "ORDER BY m.matchDate ASC")
    List<Match> findUpcomingMatchesByLeague(
        @Param("leagueId") UUID leagueId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("status") MatchStatusType status
    );
    
    @Query("SELECT m FROM Match m " +
           "JOIN FETCH m.league l " +
           "JOIN FETCH m.homeTeam " +
           "JOIN FETCH m.awayTeam " +
           "WHERE m.league.externalId = :leagueExternalId " +
           "AND m.matchDate >= :fromDate " +
           "AND m.status = :status " +
           "ORDER BY m.matchDate ASC")
    List<Match> findUpcomingMatchesByLeagueExternalId(
        @Param("leagueExternalId") String leagueExternalId,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("status") MatchStatusType status
    );
}
