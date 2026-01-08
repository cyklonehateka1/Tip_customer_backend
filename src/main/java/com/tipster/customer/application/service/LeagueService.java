package com.tipster.customer.application.service;

import com.tipster.customer.domain.models.dto.LeagueResponse;

import java.util.List;

public interface LeagueService {
    List<LeagueResponse> getFootballLeagues();
    List<LeagueResponse> getLeaguesBySportGroup(String sportGroup);
}
