package com.tipster.customer.application.service;

import java.time.OffsetDateTime;

/**
 * Service for syncing matches and odds from external APIs to the database
 */
public interface MatchSyncService {
    
    /**
     * Sync matches for a specific league within a date range
     * 
     * @param leagueExternalId The external ID of the league (e.g., "soccer_epl")
     * @param startDate Start date for the date range (inclusive)
     * @param endDate End date for the date range (inclusive)
     * @return Number of matches synced
     */
    int syncMatchesForLeague(String leagueExternalId, OffsetDateTime startDate, OffsetDateTime endDate);
    
    /**
     * Sync matches for a specific league for the next N days
     * 
     * @param leagueExternalId The external ID of the league
     * @param days Number of days ahead to sync
     * @return Number of matches synced
     */
    int syncMatchesForLeague(String leagueExternalId, int days);
    
    /**
     * Sync matches for all active leagues for the next N days
     * 
     * @param days Number of days ahead to sync
     * @return Total number of matches synced across all leagues
     */
    int syncMatchesForAllActiveLeagues(int days);
}
