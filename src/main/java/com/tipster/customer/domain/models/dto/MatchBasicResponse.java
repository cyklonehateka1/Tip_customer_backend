package com.tipster.customer.domain.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Basic match response for regular users (without odds)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchBasicResponse {
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
}
