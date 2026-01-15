package com.tipster.customer.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tipster.customer.application.service.MatchSyncService;
import com.tipster.customer.domain.entities.League;
import com.tipster.customer.domain.entities.MatchData;
import com.tipster.customer.domain.entities.Provider;
import com.tipster.customer.domain.entities.Team;
import com.tipster.customer.domain.enums.MatchStatusType;
import com.tipster.customer.domain.repository.LeagueRepository;
import com.tipster.customer.domain.repository.MatchDataRepository;
import com.tipster.customer.domain.repository.ProviderRepository;
import com.tipster.customer.domain.repository.TeamRepository;
import com.tipster.customer.infrastructure.external.theoddsapi.TheOddsApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service implementation for syncing matches from The Odds API to the database
 * Works with top 5 European leagues only
 * Handles cases where markets may not exist for some leagues gracefully
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSyncServiceImpl implements MatchSyncService {

    private final TheOddsApiClient oddsApiClient;
    private final MatchDataRepository matchDataRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final ProviderRepository providerRepository;
    private final ObjectMapper objectMapper;

    // Top 5 European leagues
    private static final List<String> TOP_5_EUROPEAN_LEAGUES = List.of(
            "soccer_epl",                    // Premier League - England
            "soccer_spain_la_liga",          // La Liga - Spain
            "soccer_italy_serie_a",          // Serie A - Italy
            "soccer_germany_bundesliga",     // Bundesliga - Germany
            "soccer_france_ligue_one"        // Ligue 1 - France
    );

    // Use simple h2h market - most common and reliable
    private static final String DEFAULT_REGIONS = "us,uk,eu";
    private static final String H2H_MARKET = "h2h";
    
    // Date formatting for The Odds API (YYYY-MM-DDTHH:MM:SSZ, no fractional seconds)
    private static final DateTimeFormatter ODDS_API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public int syncMatchesForAllActiveLeagues(int days) {
        log.info("Starting match sync for top 5 European leagues (next {} days)", days);
        
        OffsetDateTime startDate = OffsetDateTime.now();
        OffsetDateTime endDate = startDate.plusDays(days);
        
        int totalSyncedCount = 0;
        int totalErrorCount = 0;
        
        for (String leagueExternalId : TOP_5_EUROPEAN_LEAGUES) {
            try {
                int syncedCount = syncMatchesForLeague(leagueExternalId, startDate, endDate);
                totalSyncedCount += syncedCount;
                log.info("Successfully synced {} matches for league: {}", syncedCount, leagueExternalId);
            } catch (Exception e) {
                totalErrorCount++;
                log.error("Error syncing matches for league {}: {}", leagueExternalId, e.getMessage(), e);
                // Continue with other leagues even if one fails
            }
        }
        
        if (totalErrorCount > 0) {
            log.warn("Failed to sync {} leagues out of {} total", totalErrorCount, TOP_5_EUROPEAN_LEAGUES.size());
        }
        
        log.info("Successfully synced {} matches across {} leagues", totalSyncedCount, TOP_5_EUROPEAN_LEAGUES.size() - totalErrorCount);
        return totalSyncedCount;
    }

    @Override
    public int syncMatchesForLeague(String leagueExternalId, int days) {
        OffsetDateTime startDate = OffsetDateTime.now();
        OffsetDateTime endDate = startDate.plusDays(days);
        return syncMatchesForLeague(leagueExternalId, startDate, endDate);
    }

    @Override
    public int syncMatchesForLeague(String leagueExternalId, OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Starting match sync for league: {} from {} to {}", leagueExternalId, startDate, endDate);
        
        try {
            // Find league in database
            Provider theOddsApiProvider = providerRepository.findByCode("THE_ODDS_API")
                    .orElseThrow(() -> new RuntimeException("THE_ODDS_API provider not found in database"));
            
            League league = leagueRepository.findByProviderAndExternalId(theOddsApiProvider, leagueExternalId)
                    .orElseThrow(() -> new RuntimeException("League not found: " + leagueExternalId));

            // Format dates for API (The Odds API requires YYYY-MM-DDTHH:MM:SSZ format, no fractional seconds)
            String startDateStr = startDate.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).format(ODDS_API_DATE_FORMATTER);
            String endDateStr = endDate.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).format(ODDS_API_DATE_FORMATTER);

            // Fetch matches from API using h2h market (most common and reliable)
            log.debug("Fetching matches for league: {} using h2h market", leagueExternalId);
            String matchesJson;
            try {
                matchesJson = oddsApiClient.fetchOdds(
                        leagueExternalId,
                        DEFAULT_REGIONS,
                        H2H_MARKET,
                        startDateStr,
                        endDateStr,
                        "decimal"
                );
            } catch (Exception e) {
                log.error("Error fetching matches from API for league {}: {}", leagueExternalId, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch matches from API for league: " + leagueExternalId, e);
            }
            
            // Handle empty or null response gracefully
            if (matchesJson == null || matchesJson.trim().isEmpty()) {
                log.warn("Empty response from API for league: {}", leagueExternalId);
                return 0;
            }
            
            // Parse matches from JSON
            List<JsonNode> matches = parseMatchesJson(matchesJson);
            log.info("Parsed {} matches from API for league: {}", matches.size(), leagueExternalId);

            // Process and save matches (each in its own transaction to prevent one failure from aborting all)
            int syncedCount = 0;
            int errorCount = 0;
            for (JsonNode matchData : matches) {
                try {
                    MatchData match = processAndSaveMatchInNewTransaction(matchData, league);
                    if (match != null) {
                        syncedCount++;
                    } else {
                        errorCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    String matchId = matchData.path("id").asText("unknown");
                    log.error("Error processing match {} for league {}: {}", matchId, leagueExternalId, e.getMessage());
                    // Log full stack trace in debug mode
                    if (log.isDebugEnabled()) {
                        log.debug("Full error details for match {}: ", matchId, e);
                    }
                    // Continue processing other matches - each match has its own transaction
                }
            }
            
            if (errorCount > 0) {
                log.warn("Failed to process {} matches for league: {}", errorCount, leagueExternalId);
            }

            log.info("Successfully synced {} matches for league: {}", syncedCount, leagueExternalId);
            return syncedCount;

        } catch (Exception e) {
            log.error("Error syncing matches for league {}: {}", leagueExternalId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync matches for league: " + leagueExternalId, e);
        }
    }

    /**
     * Parse JSON string to list of match nodes
     * Handles errors gracefully and returns empty list on failure
     */
    private List<JsonNode> parseMatchesJson(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                log.warn("Empty JSON response from API");
                return Collections.emptyList();
            }
            
            JsonNode root = objectMapper.readTree(json);
            if (root.isArray()) {
                List<JsonNode> matches = new ArrayList<>();
                for (JsonNode match : root) {
                    matches.add(match);
                }
                log.debug("Parsed {} matches from JSON response", matches.size());
                return matches;
            } else {
                // API returned an object (likely an error response)
                if (root.has("message")) {
                    log.warn("API returned error object. Message: {}", root.get("message").asText());
                } else if (root.has("error")) {
                    log.warn("API returned error object. Error: {}", root.get("error").asText());
                } else {
                    String responseContent = json.length() > 500 ? json.substring(0, 500) : json;
                    log.warn("API returned object instead of array. Response content: {}", responseContent);
                }
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error parsing matches JSON: {}. First 500 chars: {}", 
                    e.getMessage(), json != null && json.length() > 500 ? json.substring(0, 500) : json, e);
            return Collections.emptyList();
        }
    }

    /**
     * Process a match from The Odds API and save it to the database
     * Each match is processed in its own transaction to prevent one failure from aborting all
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public MatchData processAndSaveMatchInNewTransaction(JsonNode matchData, League league) {
        return processAndSaveMatch(matchData, league);
    }

    /**
     * Process a match from The Odds API and save it to the database
     * This method handles all validation and error cases gracefully
     */
    private MatchData processAndSaveMatch(JsonNode matchData, League league) {
        try {
            // Extract external ID (required)
            String externalId = matchData.path("id").asText();
            if (externalId == null || externalId.isEmpty()) {
                log.warn("Match missing external ID, skipping");
                return null;
            }

            // Check if match already exists
            Optional<MatchData> existingMatchOpt = matchDataRepository.findByExternalId(externalId);
            MatchData match = existingMatchOpt.orElse(new MatchData());
            
            // Set external ID
            match.setExternalId(externalId);

            // Set league
            match.setLeague(league);

            // Parse and set match datetime (required)
            String commenceTimeStr = matchData.path("commence_time").asText();
            if (commenceTimeStr == null || commenceTimeStr.isEmpty()) {
                log.warn("Match {} missing commence_time, skipping", externalId);
                return null;
            }
            
            try {
                OffsetDateTime matchDatetime = OffsetDateTime.parse(commenceTimeStr, ISO_FORMATTER);
                match.setMatchDatetime(matchDatetime);
            } catch (Exception e) {
                log.warn("Match {} has invalid commence_time format: {}, skipping", externalId, commenceTimeStr);
                return null;
            }

            // Extract team names (required)
            String homeTeamName = matchData.path("home_team").asText();
            String awayTeamName = matchData.path("away_team").asText();

            if (homeTeamName == null || homeTeamName.isEmpty() || 
                awayTeamName == null || awayTeamName.isEmpty()) {
                log.warn("Match {} missing team names (home: {}, away: {}), skipping", 
                        externalId, homeTeamName, awayTeamName);
                return null;
            }

            // Get or create teams with retry logic
            // Get country from league before setting it on match to avoid lazy loading issues
            String country = league != null ? league.getCountry() : null;
            Team homeTeam;
            Team awayTeam;
            try {
                homeTeam = getOrCreateTeamWithRetry(homeTeamName, country);
                awayTeam = getOrCreateTeamWithRetry(awayTeamName, country);
            } catch (Exception e) {
                log.error("Error getting/creating teams for match {}: {}", externalId, e.getMessage(), e);
                throw new RuntimeException("Failed to get/create teams for match: " + externalId, e);
            }

            match.setHomeTeam(homeTeam);
            match.setAwayTeam(awayTeam);

            // Set status (default to scheduled)
            match.setStatus(MatchStatusType.scheduled);

            // Update last synced timestamp
            match.setLastSyncedAt(OffsetDateTime.now());

            // Save match to database
            try {
                match = matchDataRepository.save(match);
                log.debug("Saved match: {} ({}) - {} vs {}", match.getId(), externalId, homeTeamName, awayTeamName);
                return match;
            } catch (DataIntegrityViolationException e) {
                // Handle unique constraint violations (external_id already exists)
                log.warn("Match {} already exists in database (unique constraint violation), fetching existing match", externalId);
                Optional<MatchData> existingMatch = matchDataRepository.findByExternalId(externalId);
                if (existingMatch.isPresent()) {
                    return existingMatch.get();
                }
                throw new RuntimeException("Failed to save match due to constraint violation: " + externalId, e);
            } catch (Exception e) {
                log.error("Error saving match {} ({} vs {}): {}", externalId, homeTeamName, awayTeamName, e.getMessage(), e);
                throw new RuntimeException("Failed to save match: " + externalId, e);
            }

        } catch (RuntimeException e) {
            // Re-throw runtime exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing match: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error processing match", e);
        }
    }

    /**
     * Get or create a team by name with retry logic
     * Handles race conditions by retrying fetch if creation fails due to duplicate key
     */
    private Team getOrCreateTeamWithRetry(String teamName, String country) {
        try {
            return getOrCreateTeam(teamName, country);
        } catch (DataIntegrityViolationException e) {
            // Handle duplicate key or unique constraint violation
            // This can happen in race conditions - another thread/process created the team
            log.debug("Team {} already exists (race condition), fetching from DB", teamName);
            String externalId = generateTeamExternalId(teamName);
            Team existingTeam = fetchExistingTeam(teamName, externalId);
            if (existingTeam != null) {
                return existingTeam;
            }
            // If we still can't find it, log error and rethrow
            log.error("Failed to create team {} and could not find existing team after duplicate key violation", teamName);
            throw new RuntimeException("Failed to create team: " + teamName, e);
        }
    }

    /**
     * Get or create a team by name
     * Uses REQUIRES_NEW propagation to isolate transaction failures
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Team getOrCreateTeam(String teamName, String country) {
        // Try to find by name first
        Optional<Team> teamOpt = teamRepository.findByName(teamName);
        if (teamOpt.isPresent()) {
            return teamOpt.get();
        }

        // Generate external ID
        String externalId = generateTeamExternalId(teamName);
        
        // Try to find by external ID (in case name changed but external ID exists)
        Optional<Team> teamByExternalIdOpt = teamRepository.findByExternalId(externalId);
        if (teamByExternalIdOpt.isPresent()) {
            Team existingTeam = teamByExternalIdOpt.get();
            // Update name if it changed (but don't save if name is the same to avoid unnecessary DB call)
            if (!teamName.equals(existingTeam.getName())) {
                existingTeam.setName(teamName);
                existingTeam = teamRepository.save(existingTeam);
            }
            return existingTeam;
        }

        // Create new team
        Team team = new Team();
        team.setName(teamName);
        team.setCountry(country);
        team.setExternalId(externalId);

        team = teamRepository.save(team);
        log.debug("Created new team: {} ({})", team.getName(), team.getId());
        return team;
    }

    /**
     * Generate external ID for a team
     */
    private String generateTeamExternalId(String teamName) {
        return "team_" + teamName.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }

    /**
     * Fetch an existing team by name or external ID
     * Runs in a new transaction to recover from aborted transactions
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private Team fetchExistingTeam(String teamName, String externalId) {
        Optional<Team> teamOpt = teamRepository.findByName(teamName);
        if (teamOpt.isPresent()) {
            return teamOpt.get();
        }
        
        Optional<Team> teamByExternalIdOpt = teamRepository.findByExternalId(externalId);
        if (teamByExternalIdOpt.isPresent()) {
            return teamByExternalIdOpt.get();
        }
        
        return null;
    }
}
