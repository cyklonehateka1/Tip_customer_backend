package com.tipster.customer.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tipster.customer.application.service.MatchSyncService;
import com.tipster.customer.domain.entities.League;
import com.tipster.customer.domain.entities.Match;
import com.tipster.customer.domain.entities.Provider;
import com.tipster.customer.domain.entities.Team;
import com.tipster.customer.domain.enums.MatchStatusType;
import com.tipster.customer.domain.repository.LeagueRepository;
import com.tipster.customer.domain.repository.MatchRepository;
import com.tipster.customer.domain.repository.ProviderRepository;
import com.tipster.customer.domain.repository.TeamRepository;
import com.tipster.customer.infrastructure.external.theoddsapi.TheOddsApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service implementation for syncing matches and odds from The Odds API to the database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSyncServiceImpl implements MatchSyncService {

    private final TheOddsApiClient oddsApiClient;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final ProviderRepository providerRepository;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_REGIONS = "us,uk,eu";
    private static final String MAIN_MARKETS = "h2h,spreads,totals";
    private static final String ADDITIONAL_MARKETS = "btts,double_chance,alternate_totals,alternate_spreads";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    @Transactional
    public int syncMatchesForLeague(String leagueExternalId, OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Starting match sync for league: {} from {} to {}", leagueExternalId, startDate, endDate);
        
        try {
            // Find league in database
            Provider theOddsApiProvider = providerRepository.findByCode("THE_ODDS_API")
                    .orElseThrow(() -> new RuntimeException("THE_ODDS_API provider not found in database"));
            
            League league = leagueRepository.findByProviderAndExternalId(theOddsApiProvider, leagueExternalId)
                    .orElseThrow(() -> new RuntimeException("League not found: " + leagueExternalId));

            // Format dates for API
            String startDateStr = startDate.format(ISO_FORMATTER);
            String endDateStr = endDate.format(ISO_FORMATTER);

            // Fetch main markets
            log.debug("Fetching main markets for league: {}", leagueExternalId);
            String mainMarketsJson = oddsApiClient.fetchOdds(
                    leagueExternalId,
                    DEFAULT_REGIONS,
                    MAIN_MARKETS,
                    startDateStr,
                    endDateStr,
                    "decimal"
            );

            // Fetch additional markets
            log.debug("Fetching additional markets for league: {}", leagueExternalId);
            String additionalMarketsJson = oddsApiClient.fetchOdds(
                    leagueExternalId,
                    DEFAULT_REGIONS,
                    ADDITIONAL_MARKETS,
                    startDateStr,
                    endDateStr,
                    "decimal"
            );

            // Parse and merge results
            List<JsonNode> mainMatches = parseMatchesJson(mainMarketsJson);
            List<JsonNode> additionalMatches = parseMatchesJson(additionalMarketsJson);

            // Merge odds data by combining bookmakers from both responses
            Map<String, JsonNode> matchesMap = new HashMap<>();
            
            // First, add all main matches
            for (JsonNode match : mainMatches) {
                String matchId = match.path("id").asText();
                matchesMap.put(matchId, match);
            }

            // Then merge additional markets into main matches
            for (JsonNode additionalMatch : additionalMatches) {
                String matchId = additionalMatch.path("id").asText();
                JsonNode mainMatch = matchesMap.get(matchId);
                
                if (mainMatch != null) {
                    // Merge bookmakers from additional markets into main match
                    JsonNode mergedMatch = mergeMatchOdds(mainMatch, additionalMatch);
                    matchesMap.put(matchId, mergedMatch);
                } else {
                    // New match found only in additional markets (shouldn't happen, but handle it)
                    matchesMap.put(matchId, additionalMatch);
                }
            }

            // Process and save matches
            int syncedCount = 0;
            for (JsonNode matchData : matchesMap.values()) {
                try {
                    Match match = processAndSaveMatch(matchData, league);
                    if (match != null) {
                        syncedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error processing match {}: {}", matchData.path("id").asText(), e.getMessage(), e);
                }
            }

            log.info("Successfully synced {} matches for league: {}", syncedCount, leagueExternalId);
            return syncedCount;

        } catch (Exception e) {
            log.error("Error syncing matches for league {}: {}", leagueExternalId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync matches: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int syncMatchesForLeague(String leagueExternalId, int days) {
        OffsetDateTime startDate = OffsetDateTime.now();
        OffsetDateTime endDate = startDate.plusDays(days);
        return syncMatchesForLeague(leagueExternalId, startDate, endDate);
    }

    @Override
    @Transactional
    public int syncMatchesForAllActiveLeagues(int days) {
        log.info("Starting match sync for all active leagues for next {} days", days);
        
        Provider theOddsApiProvider = providerRepository.findByCode("THE_ODDS_API")
                .orElseThrow(() -> new RuntimeException("THE_ODDS_API provider not found in database"));
        
        List<League> allLeagues = leagueRepository.findByProvider(theOddsApiProvider);
        List<League> activeLeagues = allLeagues.stream()
                .filter(league -> Boolean.TRUE.equals(league.getIsActive()))
                .toList();
        
        int totalSynced = 0;
        for (League league : activeLeagues) {
            try {
                if (league.getExternalId() != null) {
                    int count = syncMatchesForLeague(league.getExternalId(), days);
                    totalSynced += count;
                    log.debug("Synced {} matches for league: {}", count, league.getName());
                }
            } catch (Exception e) {
                log.error("Error syncing matches for league {}: {}", league.getName(), e.getMessage(), e);
            }
        }
        
        log.info("Total matches synced across all leagues: {}", totalSynced);
        return totalSynced;
    }

    /**
     * Parse JSON string to list of match nodes
     */
    private List<JsonNode> parseMatchesJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.isArray()) {
                List<JsonNode> matches = new ArrayList<>();
                for (JsonNode match : root) {
                    matches.add(match);
                }
                return matches;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error parsing matches JSON: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Merge odds from additional markets into main match
     * Creates a new merged JSON node
     */
    private JsonNode mergeMatchOdds(JsonNode mainMatch, JsonNode additionalMatch) {
        try {
            // Create a mutable copy of the main match
            Map<String, Object> mergedMap = objectMapper.convertValue(mainMatch, Map.class);
            
            // Get bookmakers from both matches
            List<Map<String, Object>> mainBookmakers = (List<Map<String, Object>>) mergedMap.get("bookmakers");
            List<Map<String, Object>> additionalBookmakers = objectMapper.convertValue(
                    additionalMatch.path("bookmakers"), 
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            if (mainBookmakers == null) {
                mainBookmakers = new ArrayList<>();
                mergedMap.put("bookmakers", mainBookmakers);
            }

            // Create a map of bookmakers by key for quick lookup
            Map<String, Map<String, Object>> bookmakerMap = new HashMap<>();
            for (Map<String, Object> bookmaker : mainBookmakers) {
                String key = (String) bookmaker.get("key");
                if (key != null) {
                    bookmakerMap.put(key, bookmaker);
                }
            }

            // Merge additional markets into existing bookmakers
            for (Map<String, Object> additionalBookmaker : additionalBookmakers) {
                String key = (String) additionalBookmaker.get("key");
                if (key == null) continue;

                Map<String, Object> existingBookmaker = bookmakerMap.get(key);
                
                if (existingBookmaker != null) {
                    // Merge markets
                    List<Map<String, Object>> existingMarkets = (List<Map<String, Object>>) existingBookmaker.get("markets");
                    List<Map<String, Object>> additionalMarkets = (List<Map<String, Object>>) additionalBookmaker.get("markets");
                    
                    if (existingMarkets == null) {
                        existingMarkets = new ArrayList<>();
                        existingBookmaker.put("markets", existingMarkets);
                    }
                    
                    if (additionalMarkets != null) {
                        // Add additional markets to existing markets
                        existingMarkets.addAll(additionalMarkets);
                    }
                } else {
                    // New bookmaker, add it
                    mainBookmakers.add(additionalBookmaker);
                    bookmakerMap.put(key, additionalBookmaker);
                }
            }

            // Convert back to JsonNode
            return objectMapper.valueToTree(mergedMap);
        } catch (Exception e) {
            log.error("Error merging match odds: {}", e.getMessage(), e);
            // Return main match if merge fails
            return mainMatch;
        }
    }

    /**
     * Process a match from The Odds API and save it to the database
     */
    private Match processAndSaveMatch(JsonNode matchData, League league) {
        try {
            String externalId = matchData.path("id").asText();
            if (externalId == null || externalId.isEmpty()) {
                log.warn("Match missing external ID, skipping");
                return null;
            }

            // Check if match already exists
            Optional<Match> existingMatchOpt = matchRepository.findByExternalId(externalId);
            Match match = existingMatchOpt.orElse(new Match());
            
            // Set external ID
            match.setExternalId(externalId);

            // Set league
            match.setLeague(league);

            // Parse and set match date
            String commenceTimeStr = matchData.path("commence_time").asText();
            if (commenceTimeStr != null && !commenceTimeStr.isEmpty()) {
                OffsetDateTime matchDate = OffsetDateTime.parse(commenceTimeStr, ISO_FORMATTER);
                match.setMatchDate(matchDate);
            } else {
                log.warn("Match {} missing commence_time, skipping", externalId);
                return null;
            }

            // Get or create teams
            String homeTeamName = matchData.path("home_team").asText();
            String awayTeamName = matchData.path("away_team").asText();

            if (homeTeamName == null || homeTeamName.isEmpty() || 
                awayTeamName == null || awayTeamName.isEmpty()) {
                log.warn("Match {} missing team names, skipping", externalId);
                return null;
            }

            Team homeTeam = getOrCreateTeam(homeTeamName, league.getCountry());
            Team awayTeam = getOrCreateTeam(awayTeamName, league.getCountry());

            match.setHomeTeam(homeTeam);
            match.setAwayTeam(awayTeam);

            // Set status (default to SCHEDULED, can be updated later)
            match.setStatus(MatchStatusType.SCHEDULED);

            // Update last synced timestamp
            match.setLastSyncedAt(OffsetDateTime.now());

            // Store odds as JSON
            String oddsJson = matchData.toString();
            match.setOddsJson(oddsJson);

            // Save match
            match = matchRepository.save(match);
            log.debug("Saved match: {} ({})", match.getId(), externalId);

            return match;

        } catch (Exception e) {
            log.error("Error processing match: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get or create a team by name
     */
    private Team getOrCreateTeam(String teamName, String country) {
        // Try to find by name first
        Optional<Team> teamOpt = teamRepository.findByName(teamName);
        if (teamOpt.isPresent()) {
            return teamOpt.get();
        }

        // Create new team
        Team team = new Team();
        team.setName(teamName);
        team.setCountry(country);
        // Generate a simple external ID (can be improved)
        team.setExternalId("team_" + teamName.toLowerCase().replaceAll("[^a-z0-9]", "_"));

        team = teamRepository.save(team);
        log.debug("Created new team: {} ({})", team.getName(), team.getId());
        return team;
    }
}
