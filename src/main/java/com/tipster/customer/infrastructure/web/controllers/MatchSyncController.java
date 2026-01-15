package com.tipster.customer.infrastructure.web.controllers;

import com.tipster.customer.application.service.MatchSyncService;
import com.tipster.customer.domain.models.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * Controller for manually triggering match sync operations
 * Note: In production, this should be called by scheduled jobs, not manually
 */
@RestController
@RequestMapping("/matches/sync")
@RequiredArgsConstructor
public class MatchSyncController {

    private final MatchSyncService matchSyncService;

    /**
     * Sync matches for a specific league
     * 
     * @param leagueExternalId League external ID (e.g., "soccer_epl")
     * @param days Number of days ahead to sync (default: 7)
     */
    @PostMapping("/league/{leagueExternalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> syncLeagueMatches(
            @PathVariable String leagueExternalId,
            @RequestParam(defaultValue = "7") int days) {
        
        int syncedCount = matchSyncService.syncMatchesForLeague(leagueExternalId, days);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Successfully synced " + syncedCount + " matches for league: " + leagueExternalId,
                syncedCount
        ));
    }

    /**
     * Sync matches for a specific league within a date range
     * 
     * @param leagueExternalId League external ID
     * @param startDate Start date (ISO 8601 format)
     * @param endDate End date (ISO 8601 format)
     */
    @PostMapping("/league/{leagueExternalId}/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> syncLeagueMatchesInRange(
            @PathVariable String leagueExternalId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        OffsetDateTime start = OffsetDateTime.parse(startDate);
        OffsetDateTime end = OffsetDateTime.parse(endDate);
        
        int syncedCount = matchSyncService.syncMatchesForLeague(leagueExternalId, start, end);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Successfully synced " + syncedCount + " matches for league: " + leagueExternalId,
                syncedCount
        ));
    }

    /**
     * Sync matches for all active leagues
     * 
     * @param days Number of days ahead to sync (default: 7)
     */
    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> syncAllLeagues(
            @RequestParam(defaultValue = "7") int days) {
        
        int totalSynced = matchSyncService.syncMatchesForAllActiveLeagues(days);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Successfully synced " + totalSynced + " matches across all active leagues",
                totalSynced
        ));
    }
}
