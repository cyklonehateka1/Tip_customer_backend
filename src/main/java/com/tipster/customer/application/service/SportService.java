package com.tipster.customer.application.service;

import com.tipster.customer.domain.models.dto.SportResponse;

import java.util.List;

public interface SportService {
    List<SportResponse> syncSportsFromProvider(String providerCode);
    List<SportResponse> getAllSports();
    List<SportResponse> getSportsByGroup(String sportGroup);
}
