package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.MatchData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
