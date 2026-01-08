package com.tipster.customer.domain.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResponse {
    private UUID id;
    private String externalId;
    private String name;
    private String description;
    private String country;
    private String logoUrl;
    private Boolean isActive;
    private String sportKey;
    private String sportGroup;
}
