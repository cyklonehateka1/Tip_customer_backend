package com.tipster.customer.domain.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String country;
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String logoUrl;
    
    private Boolean isActive;
    private String sportKey;
    private String sportGroup;
}
