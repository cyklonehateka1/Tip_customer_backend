package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.League;
import com.tipster.customer.domain.entities.Provider;
import com.tipster.customer.domain.entities.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeagueRepository extends JpaRepository<League, UUID> {
    Optional<League> findByProviderAndExternalId(Provider provider, String externalId);
    boolean existsByProviderAndExternalId(Provider provider, String externalId);
    List<League> findByProvider(Provider provider);
    List<League> findBySport(Sport sport);
    List<League> findByIsActive(Boolean isActive);
    
    /**
     * Optimized query to fetch active leagues by sport group in a single query.
     * Uses JOIN FETCH to eagerly load the sport relationship, avoiding N+1 queries.
     */
    @Query("SELECT l FROM League l " +
           "JOIN FETCH l.sport s " +
           "WHERE s.sportGroup = :sportGroup " +
           "AND l.isActive = true " +
           "ORDER BY l.name ASC")
    List<League> findActiveLeaguesBySportGroupWithSport(@Param("sportGroup") String sportGroup);
}
