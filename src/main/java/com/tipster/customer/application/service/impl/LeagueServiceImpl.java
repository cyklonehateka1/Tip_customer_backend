package com.tipster.customer.application.service.impl;

import com.tipster.customer.application.service.LeagueService;
import com.tipster.customer.domain.entities.League;
import com.tipster.customer.domain.models.dto.LeagueResponse;
import com.tipster.customer.domain.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;

    private static final String SOCCER_GROUP = "Soccer";
    private static final String CACHE_NAME = "leagues";

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME, key = "'football'", unless = "#result.isEmpty()")
    public List<LeagueResponse> getFootballLeagues() {
        return getLeaguesBySportGroup(SOCCER_GROUP);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME, key = "#sportGroup", unless = "#result.isEmpty()")
    public List<LeagueResponse> getLeaguesBySportGroup(String sportGroup) {
        log.debug("Fetching leagues for sport group: {} from database", sportGroup);

        // Single optimized query with JOIN FETCH to avoid N+1 problem
        // This fetches all active leagues with their sport relationship in one query
        List<League> leagues = leagueRepository.findActiveLeaguesBySportGroupWithSport(sportGroup);

        log.debug("Found {} active leagues in DB for sport group: {}", leagues.size(), sportGroup);
        
        return leagues.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LeagueResponse mapToResponse(League league) {
        LeagueResponse response = new LeagueResponse();
        response.setId(league.getId());
        response.setExternalId(league.getExternalId());
        response.setName(league.getName());
        response.setDescription(null);
        
        // Country is included in the response for all leagues
        // Country may be null for international tournaments (e.g., UEFA Champions League)
        response.setCountry(league.getCountry());
        response.setLogoUrl(league.getLogoUrl());
        
        response.setIsActive(league.getIsActive());
        
        // Sport is already eagerly loaded via JOIN FETCH, no additional query needed
        if (league.getSport() != null) {
            response.setSportKey(league.getSport().getSportKey());
            response.setSportGroup(league.getSport().getSportGroup());
        }
        
        return response;
    }
}
