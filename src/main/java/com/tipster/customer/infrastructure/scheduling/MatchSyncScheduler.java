package com.tipster.customer.infrastructure.scheduling;

import com.tipster.customer.application.service.MatchSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled jobs for automatically syncing matches from The Odds API
 * 
 * Multiple sync types with different date ranges:
 * - 24-hour sync: Frequent updates for upcoming matches (next 1 day)
 * - Weekly sync: Regular sync for upcoming matches (next 7 days) - DEFAULT ENABLED
 * - Monthly sync: Extended sync for future matches (next 30 days)
 * 
 * Configuration:
 * - match.sync.enabled: Enable/disable all scheduled syncs (default: true)
 * - match.sync.24h.enabled: Enable 24-hour sync (default: false)
 * - match.sync.weekly.enabled: Enable weekly sync (default: true)
 * - match.sync.monthly.enabled: Enable monthly sync (default: false)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "match.sync.enabled", havingValue = "true", matchIfMissing = true)
public class MatchSyncScheduler {

    private final MatchSyncService matchSyncService;

    /**
     * 24-Hour Sync: Frequent updates for upcoming matches (next 24 hours)
     * Runs every 2 hours
     * Purpose: Keep odds fresh for matches happening soon
     */
    @Scheduled(fixedDelayString = "${match.sync.24h.interval-milliseconds:7200000}", initialDelay = 180000)
    @ConditionalOnProperty(name = "match.sync.24h.enabled", havingValue = "true", matchIfMissing = false)
    public void sync24HourMatches() {
        log.info("Starting 24-hour match sync (next 1 day)");
        
        long startTime = System.currentTimeMillis();
        
        try {
            int totalSynced = matchSyncService.syncMatchesForAllActiveLeagues(1);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed 24-hour match sync: {} matches synced in {} ms", totalSynced, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error during 24-hour match sync after {} ms: {}", duration, e.getMessage(), e);
        }
    }

    /**
     * Weekly Sync: Regular sync for upcoming matches (next 7 days)
     * Runs every 6 hours
     * Purpose: Main sync to populate database with upcoming matches
     * DEFAULT: ENABLED
     */
    @Scheduled(fixedDelayString = "${match.sync.weekly.interval-milliseconds:21600000}", initialDelay = 60000)
    @ConditionalOnProperty(name = "match.sync.weekly.enabled", havingValue = "true", matchIfMissing = true)
    public void syncWeeklyMatches() {
        log.info("Starting weekly match sync (next 7 days)");
        
        long startTime = System.currentTimeMillis();
        
        try {
            int totalSynced = matchSyncService.syncMatchesForAllActiveLeagues(7);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed weekly match sync: {} matches synced in {} ms", totalSynced, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error during weekly match sync after {} ms: {}", duration, e.getMessage(), e);
        }
    }

    /**
     * Monthly Sync: Extended sync for future matches (next 30 days)
     * Runs every 12 hours
     * Purpose: Get future matches scheduled well in advance
     */
    @Scheduled(fixedDelayString = "${match.sync.monthly.interval-milliseconds:43200000}", initialDelay = 240000)
    @ConditionalOnProperty(name = "match.sync.monthly.enabled", havingValue = "true", matchIfMissing = false)
    public void syncMonthlyMatches() {
        log.info("Starting monthly match sync (next 30 days)");
        
        long startTime = System.currentTimeMillis();
        
        try {
            int totalSynced = matchSyncService.syncMatchesForAllActiveLeagues(30);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed monthly match sync: {} matches synced in {} ms", totalSynced, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error during monthly match sync after {} ms: {}", duration, e.getMessage(), e);
        }
    }
}
