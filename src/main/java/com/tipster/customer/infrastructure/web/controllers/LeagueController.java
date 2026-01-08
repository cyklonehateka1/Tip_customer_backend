package com.tipster.customer.infrastructure.web.controllers;

import com.tipster.customer.application.service.LeagueService;
import com.tipster.customer.domain.models.ApiResponse;
import com.tipster.customer.domain.models.dto.LeagueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    @GetMapping("/football")
    public ResponseEntity<ApiResponse<List<LeagueResponse>>> getFootballLeagues() {
        List<LeagueResponse> leagues = leagueService.getFootballLeagues();
        return ResponseEntity.ok(ApiResponse.success(
                "Football leagues retrieved successfully", 
                leagues));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeagueResponse>>> getLeagues(
            @RequestParam(required = false) String sportGroup) {
        List<LeagueResponse> leagues;
        if (sportGroup != null && !sportGroup.isEmpty()) {
            leagues = leagueService.getLeaguesBySportGroup(sportGroup);
        } else {
            // Default to football if no sport group specified
            leagues = leagueService.getFootballLeagues();
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Leagues retrieved successfully", 
                leagues));
    }
}
