package com.tipster.customer.infrastructure.web.controllers;

import com.tipster.customer.application.service.MatchService;
import com.tipster.customer.domain.enums.UserRoleType;
import com.tipster.customer.domain.models.ApiResponse;
import com.tipster.customer.domain.models.dto.MatchBasicResponse;
import com.tipster.customer.domain.models.dto.MatchDetailedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<?>> getUpcomingMatches(
            @RequestParam(required = false) UUID leagueId,
            @RequestParam(required = false) String leagueExternalId,
            Authentication authentication) {
        
        // Check if user is a tipster (defaults to false for unauthenticated users)
        boolean isTipster = isUserTipster(authentication);
        
        List<?> matches;
        if (leagueExternalId != null && !leagueExternalId.isEmpty()) {
            matches = matchService.getUpcomingMatchesByLeagueExternalId(leagueExternalId, isTipster);
        } else {
            matches = matchService.getUpcomingMatches(leagueId, isTipster);
        }
        
        String message = isTipster 
            ? "Upcoming matches with odds retrieved successfully"
            : "Upcoming matches retrieved successfully";
        
        return ResponseEntity.ok(ApiResponse.success(message, matches));
    }

    /**
     * Check if the authenticated user is a tipster
     * Returns false for unauthenticated users (they get basic match info)
     */
    private boolean isUserTipster(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TIPSTER"));
    }
}
