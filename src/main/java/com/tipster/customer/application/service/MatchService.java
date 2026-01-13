package com.tipster.customer.application.service;

import com.tipster.customer.domain.models.dto.MatchBasicResponse;
import com.tipster.customer.domain.models.dto.MatchDetailedResponse;

import java.util.List;
import java.util.UUID;

public interface MatchService {
    /**
     * Get upcoming matches
     * @param leagueId Optional league ID to filter by
     * @param isTipster Whether the requesting user is a tipster
     * @return List of matches (basic or detailed based on user role)
     */
    List<?> getUpcomingMatches(UUID leagueId, boolean isTipster);
    
    /**
     * Get upcoming matches by league external ID
     * @param leagueExternalId League external ID to filter by
     * @param isTipster Whether the requesting user is a tipster
     * @return List of matches (basic or detailed based on user role)
     */
    List<?> getUpcomingMatchesByLeagueExternalId(String leagueExternalId, boolean isTipster);
}
