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
public class SportResponse {
    private UUID id;
    private String sportKey;
    private String title;
    private String description;
    private String sportGroup;
    private Boolean isActive;
    private Boolean hasOutrights;
}
