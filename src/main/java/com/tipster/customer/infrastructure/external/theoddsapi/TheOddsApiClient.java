package com.tipster.customer.infrastructure.external.theoddsapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tipster.customer.domain.models.dto.OddsApiSportResponse;
import com.tipster.customer.infrastructure.utils.HttpHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Client for interacting with The Odds API
 * Handles all API calls to https://api.the-odds-api.com
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TheOddsApiClient {

    private final ObjectMapper objectMapper;

    @Value("${the-odds-api.api-key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.the-odds-api.com/v4";

    /**
     * Fetches all available sports from The Odds API
     * @return List of sports/leagues from the API
     */
    public List<OddsApiSportResponse> fetchSports() {
        try {
            String url = BASE_URL + "/sports?apiKey=" + 
                    URLEncoder.encode(apiKey, StandardCharsets.UTF_8) + "&all=true";
            
            log.debug("Fetching sports from The Odds API: {}", url);
            String responseBody = HttpHelper.get(url, null);
            
            List<OddsApiSportResponse> sports = objectMapper.readValue(
                    responseBody, 
                    new TypeReference<List<OddsApiSportResponse>>() {}
            );
            
            log.info("Successfully fetched {} sports from The Odds API", sports.size());
            return sports;
        } catch (Exception e) {
            log.error("Error fetching sports from The Odds API", e);
            throw new RuntimeException("Failed to fetch sports from The Odds API: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches leagues for a specific sport from The Odds API
     * Note: The Odds API returns sports which are essentially leagues
     * This method filters by sport group (e.g., "Soccer")
     * 
     * @param sportGroup The sport group to filter by (e.g., "Soccer", "Basketball")
     * @return List of leagues for the specified sport group
     */
    public List<OddsApiSportResponse> fetchLeaguesBySportGroup(String sportGroup) {
        List<OddsApiSportResponse> allSports = fetchSports();
        return allSports.stream()
                .filter(sport -> sportGroup.equalsIgnoreCase(sport.getGroup()))
                .toList();
    }

    /**
     * Fetches upcoming matches with odds from The Odds API
     * 
     * @param sportKey The sport key (e.g., "soccer_epl", "soccer_spain_la_liga")
     * @param regions Comma-separated regions (e.g., "us", "uk", "eu")
     * @param markets Comma-separated markets (e.g., "h2h,spreads,totals")
     * @return JSON string response from The Odds API
     */
    public String fetchMatchesWithOdds(String sportKey, String regions, String markets) {
        return fetchOdds(sportKey, regions, markets, null, null, "decimal");
    }

    /**
     * Fetches matches with odds from The Odds API with date filtering
     * 
     * @param sportKey The sport key (e.g., "soccer_epl", "soccer_spain_la_liga")
     * @param regions Comma-separated regions (e.g., "us", "uk", "eu")
     * @param markets Comma-separated markets (e.g., "h2h,spreads,totals")
     * @param commenceTimeFrom Start date in ISO 8601 format (e.g., "2024-01-12T00:00:00Z")
     * @param commenceTimeTo End date in ISO 8601 format (e.g., "2024-01-14T23:59:59Z")
     * @param oddsFormat Odds format: "decimal" or "american" (default: "decimal")
     * @return JSON string response from The Odds API
     */
    public String fetchOdds(String sportKey, String regions, String markets, 
                           String commenceTimeFrom, String commenceTimeTo, String oddsFormat) {
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                    .append("/sports/")
                    .append(URLEncoder.encode(sportKey, StandardCharsets.UTF_8))
                    .append("/odds")
                    .append("?apiKey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8))
                    .append("&regions=").append(URLEncoder.encode(regions, StandardCharsets.UTF_8))
                    .append("&markets=").append(URLEncoder.encode(markets, StandardCharsets.UTF_8))
                    .append("&oddsFormat=").append(URLEncoder.encode(oddsFormat, StandardCharsets.UTF_8));
            
            // Add date filters if provided
            if (commenceTimeFrom != null && !commenceTimeFrom.isEmpty()) {
                urlBuilder.append("&commenceTimeFrom=")
                        .append(URLEncoder.encode(commenceTimeFrom, StandardCharsets.UTF_8));
            }
            if (commenceTimeTo != null && !commenceTimeTo.isEmpty()) {
                urlBuilder.append("&commenceTimeTo=")
                        .append(URLEncoder.encode(commenceTimeTo, StandardCharsets.UTF_8));
            }
            
            String url = urlBuilder.toString();
            log.debug("Fetching matches with odds from The Odds API: {}", url);
            String responseBody = HttpHelper.get(url, null);
            
            log.info("Successfully fetched matches with odds for sport: {} (markets: {})", sportKey, markets);
            return responseBody;
        } catch (Exception e) {
            log.error("Error fetching matches with odds from The Odds API for sport: {}", sportKey, e);
            throw new RuntimeException("Failed to fetch matches with odds: " + e.getMessage(), e);
        }
    }
}
