package com.tipster.customer.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tipster.customer.application.service.MatchService;
import com.tipster.customer.domain.entities.MatchData;
import com.tipster.customer.domain.enums.MatchStatusType;
import com.tipster.customer.domain.models.dto.MatchBasicResponse;
import com.tipster.customer.domain.models.dto.MatchDetailedResponse;
import com.tipster.customer.domain.repository.MatchDataRepository;
import com.tipster.customer.infrastructure.external.theoddsapi.TheOddsApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchDataRepository matchDataRepository;
    private final TheOddsApiClient oddsApiClient;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_REGIONS = "us,uk,eu";
    private static final String DEFAULT_MARKETS = "h2h,spreads,totals";

    @Override
    @Transactional(readOnly = true)
    public List<?> getUpcomingMatches(UUID leagueId, boolean isTipster) {
        OffsetDateTime now = OffsetDateTime.now();
        List<MatchData> matches;

        if (leagueId != null) {
            matches = matchDataRepository.findUpcomingMatchesByLeague(leagueId, now, MatchStatusType.scheduled);
        } else {
            matches = matchDataRepository.findUpcomingMatches(now, MatchStatusType.scheduled);
        }

        if (isTipster) {
            return matches.stream()
                    .map(this::mapToDetailedResponse)
                    .collect(Collectors.toList());
        } else {
            return matches.stream()
                    .map(this::mapToBasicResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getUpcomingMatchesByLeagueExternalId(String leagueExternalId, boolean isTipster) {
        OffsetDateTime now = OffsetDateTime.now();
        List<MatchData> matches = matchDataRepository.findUpcomingMatchesByLeagueExternalId(
                leagueExternalId, now, MatchStatusType.scheduled);

        if (isTipster) {
            return matches.stream()
                    .map(this::mapToDetailedResponse)
                    .collect(Collectors.toList());
        } else {
            return matches.stream()
                    .map(this::mapToBasicResponse)
                    .collect(Collectors.toList());
        }
    }

    private MatchBasicResponse mapToBasicResponse(MatchData match) {
        MatchBasicResponse response = new MatchBasicResponse();
        response.setId(match.getId());
        response.setExternalId(match.getExternalId());
        response.setMatchDate(match.getMatchDatetime());
        response.setStatus(match.getStatus().name());
        response.setVenue(match.getVenue());
        response.setRound(match.getRound());
        response.setSeason(match.getSeason());

        // League info
        if (match.getLeague() != null) {
            MatchBasicResponse.LeagueBasicInfo leagueInfo = new MatchBasicResponse.LeagueBasicInfo();
            leagueInfo.setId(match.getLeague().getId());
            leagueInfo.setExternalId(match.getLeague().getExternalId());
            leagueInfo.setName(match.getLeague().getName());
            leagueInfo.setLogoUrl(match.getLeague().getLogoUrl());
            leagueInfo.setCountry(match.getLeague().getCountry());
            response.setLeague(leagueInfo);
        }

        // Home team info
        if (match.getHomeTeam() != null) {
            MatchBasicResponse.TeamBasicInfo homeTeamInfo = new MatchBasicResponse.TeamBasicInfo();
            homeTeamInfo.setId(match.getHomeTeam().getId());
            homeTeamInfo.setName(match.getHomeTeam().getName());
            homeTeamInfo.setShortName(match.getHomeTeam().getShortName());
            homeTeamInfo.setLogoUrl(match.getHomeTeam().getLogoUrl());
            response.setHomeTeam(homeTeamInfo);
        }

        // Away team info
        if (match.getAwayTeam() != null) {
            MatchBasicResponse.TeamBasicInfo awayTeamInfo = new MatchBasicResponse.TeamBasicInfo();
            awayTeamInfo.setId(match.getAwayTeam().getId());
            awayTeamInfo.setName(match.getAwayTeam().getName());
            awayTeamInfo.setShortName(match.getAwayTeam().getShortName());
            awayTeamInfo.setLogoUrl(match.getAwayTeam().getLogoUrl());
            response.setAwayTeam(awayTeamInfo);
        }

        return response;
    }

    private MatchDetailedResponse mapToDetailedResponse(MatchData match) {
        MatchDetailedResponse response = new MatchDetailedResponse();
        response.setId(match.getId());
        response.setExternalId(match.getExternalId());
        response.setMatchDate(match.getMatchDatetime());
        response.setStatus(match.getStatus().name());
        response.setVenue(match.getVenue());
        response.setRound(match.getRound());
        response.setSeason(match.getSeason());

        // League info
        if (match.getLeague() != null) {
            MatchDetailedResponse.LeagueBasicInfo leagueInfo = new MatchDetailedResponse.LeagueBasicInfo();
            leagueInfo.setId(match.getLeague().getId());
            leagueInfo.setExternalId(match.getLeague().getExternalId());
            leagueInfo.setName(match.getLeague().getName());
            leagueInfo.setLogoUrl(match.getLeague().getLogoUrl());
            leagueInfo.setCountry(match.getLeague().getCountry());
            response.setLeague(leagueInfo);
        }

        // Home team info
        if (match.getHomeTeam() != null) {
            MatchDetailedResponse.TeamBasicInfo homeTeamInfo = new MatchDetailedResponse.TeamBasicInfo();
            homeTeamInfo.setId(match.getHomeTeam().getId());
            homeTeamInfo.setName(match.getHomeTeam().getName());
            homeTeamInfo.setShortName(match.getHomeTeam().getShortName());
            homeTeamInfo.setLogoUrl(match.getHomeTeam().getLogoUrl());
            response.setHomeTeam(homeTeamInfo);
        }

        // Away team info
        if (match.getAwayTeam() != null) {
            MatchDetailedResponse.TeamBasicInfo awayTeamInfo = new MatchDetailedResponse.TeamBasicInfo();
            awayTeamInfo.setId(match.getAwayTeam().getId());
            awayTeamInfo.setName(match.getAwayTeam().getName());
            awayTeamInfo.setShortName(match.getAwayTeam().getShortName());
            awayTeamInfo.setLogoUrl(match.getAwayTeam().getLogoUrl());
            response.setAwayTeam(awayTeamInfo);
        }

        // Fetch and map odds from The Odds API
        try {
            if (match.getLeague() != null && match.getLeague().getExternalId() != null) {
                MatchDetailedResponse.MatchOdds odds = fetchAndMapOdds(match.getLeague().getExternalId(), match);
                response.setOdds(odds);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch odds for match {}: {}", match.getId(), e.getMessage());
            // Set odds to null if fetch fails
            response.setOdds(null);
        }

        return response;
    }

    private MatchDetailedResponse.MatchOdds fetchAndMapOdds(String sportKey, MatchData match) {
        try {
            String oddsJson = oddsApiClient.fetchMatchesWithOdds(sportKey, DEFAULT_REGIONS, DEFAULT_MARKETS);
            JsonNode oddsArray = objectMapper.readTree(oddsJson);

            // Find the match in the odds response
            for (JsonNode oddsData : oddsArray) {
                String homeTeam = oddsData.path("home_team").asText();
                String awayTeam = oddsData.path("away_team").asText();
                
                // Match by team names (fuzzy matching)
                if (isTeamMatch(homeTeam, match.getHomeTeam().getName()) &&
                    isTeamMatch(awayTeam, match.getAwayTeam().getName())) {
                    
                    return mapOddsFromJson(oddsData);
                }
            }

            log.debug("No odds found for match: {} vs {}", match.getHomeTeam().getName(), match.getAwayTeam().getName());
            return null;
        } catch (Exception e) {
            log.error("Error fetching odds for match {}: {}", match.getId(), e.getMessage(), e);
            return null;
        }
    }

    private boolean isTeamMatch(String apiTeamName, String dbTeamName) {
        // Simple fuzzy matching - can be improved
        String apiNormalized = apiTeamName.toLowerCase().trim();
        String dbNormalized = dbTeamName.toLowerCase().trim();
        return apiNormalized.equals(dbNormalized) || 
               apiNormalized.contains(dbNormalized) || 
               dbNormalized.contains(apiNormalized);
    }

    private MatchDetailedResponse.MatchOdds mapOddsFromJson(JsonNode oddsData) {
        MatchDetailedResponse.MatchOdds matchOdds = new MatchDetailedResponse.MatchOdds();
        
        JsonNode bookmakers = oddsData.path("bookmakers");
        if (!bookmakers.isArray() || bookmakers.size() == 0) {
            return null;
        }

        // Use the first bookmaker's odds (or aggregate if needed)
        JsonNode firstBookmaker = bookmakers.get(0);
        JsonNode markets = firstBookmaker.path("markets");

        for (JsonNode market : markets) {
            String marketKey = market.path("key").asText();
            JsonNode outcomes = market.path("outcomes");

            switch (marketKey) {
                case "h2h": // Match Result (1X2)
                    matchOdds.setMatchResult(mapMatchResultOdds(outcomes));
                    break;
                case "totals": // Over/Under
                    if (matchOdds.getOverUnder() == null) {
                        matchOdds.setOverUnder(new ArrayList<>());
                    }
                    MatchDetailedResponse.OverUnderOdds overUnder = mapOverUnderOdds(market);
                    if (overUnder != null) {
                        matchOdds.getOverUnder().add(overUnder);
                    }
                    break;
                case "spreads": // Handicap
                    if (matchOdds.getHandicap() == null) {
                        matchOdds.setHandicap(new ArrayList<>());
                    }
                    MatchDetailedResponse.HandicapOdds handicap = mapHandicapOdds(market);
                    if (handicap != null) {
                        matchOdds.getHandicap().add(handicap);
                    }
                    break;
            }
        }

        // Map Both Teams to Score and Double Chance if available
        // These might be in different markets or need to be calculated
        mapAdditionalMarkets(markets, matchOdds);

        return matchOdds;
    }

    private MatchDetailedResponse.OddsValue mapMatchResultOdds(JsonNode outcomes) {
        MatchDetailedResponse.OddsValue oddsValue = new MatchDetailedResponse.OddsValue();
        
        if (!outcomes.isArray()) {
            return oddsValue;
        }
        
        for (JsonNode outcome : outcomes) {
            String name = outcome.path("name").asText().toLowerCase();
            JsonNode priceNode = outcome.path("price");
            if (!priceNode.isNumber()) {
                continue;
            }
            BigDecimal price = BigDecimal.valueOf(priceNode.asDouble());
            
            if (name.contains("home") || name.equals("1")) {
                oddsValue.setHome(price);
            } else if (name.contains("draw") || name.equals("x")) {
                oddsValue.setDraw(price);
            } else if (name.contains("away") || name.equals("2")) {
                oddsValue.setAway(price);
            }
        }
        
        return oddsValue;
    }

    private MatchDetailedResponse.OverUnderOdds mapOverUnderOdds(JsonNode market) {
        MatchDetailedResponse.OverUnderOdds overUnder = new MatchDetailedResponse.OverUnderOdds();
        
        JsonNode outcomes = market.path("outcomes");
        if (!outcomes.isArray() || outcomes.size() == 0) {
            return null;
        }
        
        JsonNode pointNode = market.path("point");
        if (pointNode.isMissingNode() || !pointNode.isNumber()) {
            return null;
        }
        
        BigDecimal point = BigDecimal.valueOf(pointNode.asDouble());
        overUnder.setLine(point);
        
        for (JsonNode outcome : outcomes) {
            String name = outcome.path("name").asText().toLowerCase();
            JsonNode priceNode = outcome.path("price");
            if (!priceNode.isNumber()) {
                continue;
            }
            BigDecimal price = BigDecimal.valueOf(priceNode.asDouble());
            
            if (name.contains("over")) {
                overUnder.setOver(price);
            } else if (name.contains("under")) {
                overUnder.setUnder(price);
            }
        }
        
        return overUnder;
    }

    private MatchDetailedResponse.HandicapOdds mapHandicapOdds(JsonNode market) {
        MatchDetailedResponse.HandicapOdds handicap = new MatchDetailedResponse.HandicapOdds();
        
        JsonNode outcomes = market.path("outcomes");
        if (!outcomes.isArray() || outcomes.size() == 0) {
            return null;
        }
        
        JsonNode pointNode = market.path("point");
        if (pointNode.isMissingNode() || !pointNode.isNumber()) {
            return null;
        }
        
        BigDecimal point = BigDecimal.valueOf(pointNode.asDouble());
        handicap.setLine(point);
        
        for (JsonNode outcome : outcomes) {
            String name = outcome.path("name").asText().toLowerCase();
            JsonNode priceNode = outcome.path("price");
            if (!priceNode.isNumber()) {
                continue;
            }
            BigDecimal price = BigDecimal.valueOf(priceNode.asDouble());
            
            if (name.contains("home") || name.equals("1")) {
                handicap.setHome(price);
            } else if (name.contains("away") || name.equals("2")) {
                handicap.setAway(price);
            }
        }
        
        return handicap;
    }

    private void mapAdditionalMarkets(JsonNode markets, MatchDetailedResponse.MatchOdds matchOdds) {
        // Both Teams to Score and Double Chance might be in separate markets
        // For now, we'll leave them null or implement if The Odds API provides them
        // You can extend this method to parse additional markets as needed
    }
}
