package com.tipster.customer.infrastructure.web.controllers;

import com.tipster.customer.application.service.SportService;
import com.tipster.customer.domain.models.ApiResponse;
import com.tipster.customer.domain.models.dto.SportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sports")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SportResponse>>> getAllSports(
            @RequestParam(required = false) String group) {
        List<SportResponse> sports;
        if (group != null && !group.isEmpty()) {
            sports = sportService.getSportsByGroup(group);
        } else {
            sports = sportService.getAllSports();
        }
        return ResponseEntity.ok(ApiResponse.success("Sports retrieved successfully", sports));
    }

    @PostMapping("/sync/{providerCode}")
    public ResponseEntity<ApiResponse<List<SportResponse>>> syncSports(
            @PathVariable String providerCode,
            Authentication authentication) {
        List<SportResponse> syncedSports = sportService.syncSportsFromProvider(providerCode);
        return ResponseEntity.ok(ApiResponse.success(
                "Sports synced successfully from provider: " + providerCode, 
                syncedSports));
    }
}
