package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.PredictionRequest;
import com.smartretail.pricemonitor.dto.PredictionResponse;

import java.util.List;

public interface PredictionService {
    PredictionResponse generatePrediction(PredictionRequest request);
    List<PredictionResponse> getPredictionsByCommodityAndMarket(Long commodityId, Long marketId);
}
