package com.tipster.customer.domain.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Detailed match response for tipsters (with odds data)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchDetailedResponse {
    private UUID id;
    private String externalId;
    private LeagueBasicInfo league;
    private TeamBasicInfo homeTeam;
    private TeamBasicInfo awayTeam;
    private OffsetDateTime matchDate;
    private String status;
    private String venue;
    private String round;
    private String season;
    
    // Odds data for tipsters
    private MatchOdds odds;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeagueBasicInfo {
        private UUID id;
        private String externalId;
        private String name;
        private String logoUrl;
        private String country;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamBasicInfo {
        private UUID id;
        private String name;
        private String shortName;
        private String logoUrl;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchOdds {
        // Match Result (1X2)
        private OddsValue matchResult; // {home: 2.5, draw: 3.2, away: 2.8}
        
        // Over/Under
        private List<OverUnderOdds> overUnder; // [{line: 2.5, over: 1.8, under: 2.0}, ...]
        
        // Both Teams to Score
        private OddsValue bothTeamsToScore; // {yes: 1.9, no: 1.9}
        
        // Double Chance
        private OddsValue doubleChance; // {homeOrDraw: 1.4, homeOrAway: 1.3, drawOrAway: 1.5}
        
        // Handicap
        private List<HandicapOdds> handicap; // [{line: -1.5, home: 2.2, away: 1.7}, ...]
        
        // Additional markets
        private Map<String, Object> otherMarkets;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OddsValue {
        private BigDecimal home;
        private BigDecimal draw;
        private BigDecimal away;
        private BigDecimal yes;
        private BigDecimal no;
        private BigDecimal homeOrDraw;
        private BigDecimal homeOrAway;
        private BigDecimal drawOrAway;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverUnderOdds {
        private BigDecimal line; // e.g., 2.5
        private BigDecimal over;
        private BigDecimal under;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandicapOdds {
        private BigDecimal line; // e.g., -1.5, +0.5
        private BigDecimal home;
        private BigDecimal away;
    }
}
