package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.AnomalyAnalysisResponse;

import java.math.BigDecimal;

public interface PriceAnomalyDetectionService {
    AnomalyAnalysisResponse detectAnomaly(Long commodityId, Long marketId, BigDecimal targetPrice);
}
