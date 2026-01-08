package com.tipster.customer.application.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tipster.customer.application.service.SportService;
import com.tipster.customer.domain.entities.Provider;
import com.tipster.customer.domain.entities.Sport;
import com.tipster.customer.domain.models.dto.OddsApiSportResponse;
import com.tipster.customer.domain.models.dto.SportResponse;
import com.tipster.customer.domain.repository.ProviderRepository;
import com.tipster.customer.domain.repository.SportRepository;
import com.tipster.customer.domain.exceptions.ResourceNotFoundException;
import com.tipster.customer.infrastructure.utils.HttpHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SportServiceImpl implements SportService {

    private final SportRepository sportRepository;
    private final ProviderRepository providerRepository;
    private final ObjectMapper objectMapper;

    @Value("${the-odds-api.api-key}")
    private String oddsApiKey;

    @Override
    @Transactional
    public List<SportResponse> syncSportsFromProvider(String providerCode) {
        log.info("Syncing sports from provider: {}", providerCode);

        // Get provider
        Provider provider = providerRepository.findByCode(providerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerCode));

        if (!provider.getIsActive()) {
            throw new RuntimeException("Provider is not active: " + providerCode);
        }

        // Build API URL
        String apiUrl = provider.getBaseUrl() + "/sports?apiKey=" + 
                URLEncoder.encode(oddsApiKey, StandardCharsets.UTF_8) + "&all=true";

        try {
            // Fetch sports from API
            String responseBody = HttpHelper.get(apiUrl, null);
            List<OddsApiSportResponse> apiSports = objectMapper.readValue(
                    responseBody, 
                    new TypeReference<List<OddsApiSportResponse>>() {}
            );

            log.info("Fetched {} sports from provider: {}", apiSports.size(), providerCode);

            // Sync sports to database
            List<SportResponse> syncedSports = new ArrayList<>();
            for (OddsApiSportResponse apiSport : apiSports) {
                Sport sport = sportRepository.findBySportKey(apiSport.getKey())
                        .orElse(new Sport());

                // Update sport data
                sport.setSportKey(apiSport.getKey());
                sport.setTitle(apiSport.getTitle());
                sport.setDescription(apiSport.getDescription());
                sport.setSportGroup(apiSport.getGroup());
                sport.setIsActive(apiSport.getActive());
                sport.setHasOutrights(apiSport.getHasOutrights());

                sport = sportRepository.save(sport);
                syncedSports.add(mapToResponse(sport));
            }

            log.info("Successfully synced {} sports", syncedSports.size());
            return syncedSports;

        } catch (Exception e) {
            log.error("Error syncing sports from provider: {}", providerCode, e);
            throw new RuntimeException("Failed to sync sports from provider: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SportResponse> getAllSports() {
        return sportRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportResponse> getSportsByGroup(String sportGroup) {
        return sportRepository.findBySportGroup(sportGroup).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SportResponse mapToResponse(Sport sport) {
        return new SportResponse(
                sport.getId(),
                sport.getSportKey(),
                sport.getTitle(),
                sport.getDescription(),
                sport.getSportGroup(),
                sport.getIsActive(),
                sport.getHasOutrights()
        );
    }
}
