package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.PriceHistoryResponse;

import java.time.LocalDate;
import java.util.List;

public interface PriceHistoryService {
    List<PriceHistoryResponse> getPriceHistory(Long commodityId, Long marketId, LocalDate startDate, LocalDate endDate);
    PriceHistoryResponse getLatestPrice(Long commodityId, Long marketId);
}
