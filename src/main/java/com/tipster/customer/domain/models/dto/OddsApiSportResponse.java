package com.tipster.customer.domain.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OddsApiSportResponse {
    @JsonProperty("key")
    private String key;

    @JsonProperty("group")
    private String group;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("has_outrights")
    private Boolean hasOutrights;
}
