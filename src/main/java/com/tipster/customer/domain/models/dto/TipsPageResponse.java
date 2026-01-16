package com.tipster.customer.domain.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipsPageResponse {
    private List<TipResponse> tips;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private long freeTipsCount;
    private long availableTipsCount;
}
