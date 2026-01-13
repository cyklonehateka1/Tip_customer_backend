package com.tipster.customer.infrastructure.scheduling;

import com.tipster.customer.application.service.MatchSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for automatically syncing matches from The Odds API
 * 
 * Configuration:
 * - match.sync.enabled: Enable/disable scheduled sync (default: true)
 * - match.sync.upcoming-days: Number of days ahead to sync (default: 7)
 * - match.sync.interval-hours: Sync interval in hours (default: 6)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "match.sync.enabled", havingValue = "true", matchIfMissing = true)
public class MatchSyncScheduler {

    private final MatchSyncService matchSyncService;

    @Value("${match.sync.upcoming-days:7}")
    private int upcomingDays;

    @Value("${match.sync.interval-hours:6}")
    private int intervalHours;

    /**
     * Scheduled job to sync matches for all active leagues
     * Runs every 6 hours by default (configurable via match.sync.interval-hours)
     * 
     * Cron expression: "0 0 */6 * * ?" = Every 6 hours at minute 0
     * Or use fixed delay: Runs 6 hours after previous completion
     */
    @Scheduled(fixedDelayString = "${match.sync.interval-milliseconds:21600000}", initialDelay = 60000)
    // Alternative: @Scheduled(cron = "${match.sync.cron:0 0 */6 * * ?}")
    public void syncMatchesForAllActiveLeagues() {
        log.info("Starting scheduled match sync for all active leagues (next {} days)", upcomingDays);
        
        long startTime = System.currentTimeMillis();
        
        try {
            int totalSynced = matchSyncService.syncMatchesForAllActiveLeagues(upcomingDays);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed scheduled match sync: {} matches synced in {} ms", totalSynced, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error during scheduled match sync after {} ms: {}", duration, e.getMessage(), e);
            // Don't throw exception - let the scheduler continue
        }
    }

    /**
     * Optional: More frequent sync for live/upcoming matches (next 24 hours)
     * Runs every 2 hours
     */
    @Scheduled(fixedDelayString = "${match.sync.live-interval-milliseconds:7200000}", initialDelay = 120000)
    @ConditionalOnProperty(name = "match.sync.live-enabled", havingValue = "true", matchIfMissing = false)
    public void syncLiveMatches() {
        log.info("Starting scheduled sync for live/upcoming matches (next 24 hours)");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Sync matches for next 24 hours (more frequent updates)
            int totalSynced = matchSyncService.syncMatchesForAllActiveLeagues(1);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed live match sync: {} matches synced in {} ms", totalSynced, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error during live match sync after {} ms: {}", duration, e.getMessage(), e);
        }
    }
}
