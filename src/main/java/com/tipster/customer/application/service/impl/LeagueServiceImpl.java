package com.tipster.customer.application.service.impl;

import com.tipster.customer.application.service.LeagueService;
import com.tipster.customer.domain.entities.League;
import com.tipster.customer.domain.entities.Sport;
import com.tipster.customer.domain.models.dto.LeagueResponse;
import com.tipster.customer.domain.repository.LeagueRepository;
import com.tipster.customer.domain.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;
    private final SportRepository sportRepository;

    private static final String SOCCER_GROUP = "Soccer";

    @Override
    @Transactional(readOnly = true)
    public List<LeagueResponse> getFootballLeagues() {
        return getLeaguesBySportGroup(SOCCER_GROUP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueResponse> getLeaguesBySportGroup(String sportGroup) {
        log.info("Fetching leagues for sport group: {} from database", sportGroup);

        // Get sports for this sport group
        List<Sport> sports = sportRepository.findBySportGroup(sportGroup);
        
        if (sports.isEmpty()) {
            log.warn("No sports found in DB for group: {}", sportGroup);
            return List.of();
        }

        // Get all leagues from DB that match the sport group
        List<League> dbLeagues = sports.stream()
                .flatMap(sport -> leagueRepository.findBySport(sport).stream())
                .filter(league -> league.getIsActive() != null && league.getIsActive())
                .collect(Collectors.toList());

        log.info("Found {} active leagues in DB for sport group: {}", dbLeagues.size(), sportGroup);
        
        return dbLeagues.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LeagueResponse mapToResponse(League league) {
        LeagueResponse response = new LeagueResponse();
        response.setId(league.getId());
        response.setExternalId(league.getExternalId());
        response.setName(league.getName());
        response.setDescription(league.getDescription());
        response.setCountry(league.getCountry());
        response.setLogoUrl(league.getLogoUrl());
        response.setIsActive(league.getIsActive());
        
        if (league.getSport() != null) {
            response.setSportKey(league.getSport().getSportKey());
            response.setSportGroup(league.getSport().getSportGroup());
        }
        
        return response;
    }
}
