package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.PredictionRequest;
import com.smartretail.pricemonitor.dto.PredictionResponse;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.Prediction;
import com.smartretail.pricemonitor.entity.PriceHistory;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.prediction.PricePredictionEngine;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.PredictionRepository;
import com.smartretail.pricemonitor.repository.PriceHistoryRepository;
import com.smartretail.pricemonitor.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PredictionRepository predictionRepository;
    private final PricePredictionEngine predictionEngine;

    @Override
    @Transactional
    public PredictionResponse generatePrediction(PredictionRequest request) {
        Commodity commodity = commodityRepository.findById(request.getCommodityId())
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", request.getCommodityId()));

        Market market = marketRepository.findById(request.getMarketId())
                .orElseThrow(() -> new ResourceNotFoundException("Market", "id", request.getMarketId()));

        List<PriceHistory> history = priceHistoryRepository.findByCommodityIdAndMarketIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                commodity.getId(), market.getId(), LocalDate.now().minusDays(90), LocalDate.now()
        );

        PricePredictionEngine.PredictionResult result = predictionEngine.predict(history, request.getTimeframe());

        Prediction prediction = predictionRepository.findByCommodityIdAndMarketIdAndTimeframe(
                commodity.getId(), market.getId(), request.getTimeframe()
        ).orElse(Prediction.builder()
                .commodity(commodity)
                .market(market)
                .timeframe(request.getTimeframe())
                .build());

        prediction.setPredictedPrice(result.predictedPrice());
        prediction.setConfidenceScore(result.confidenceScore());
        prediction.setTargetDate(result.targetDate());
        prediction.setModelVersion(result.modelVersion());

        return mapToResponse(predictionRepository.save(prediction));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredictionResponse> getPredictionsByCommodityAndMarket(Long commodityId, Long marketId) {
        return predictionRepository.findByCommodityIdAndMarketIdOrderByTargetDateDesc(commodityId, marketId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private PredictionResponse mapToResponse(Prediction p) {
        return PredictionResponse.builder()
                .id(p.getId())
                .commodityId(p.getCommodity().getId())
                .commodityName(p.getCommodity().getName())
                .marketId(p.getMarket().getId())
                .marketName(p.getMarket().getName())
                .targetDate(p.getTargetDate())
                .timeframe(p.getTimeframe())
                .predictedPrice(p.getPredictedPrice())
                .confidenceScore(p.getConfidenceScore())
                .modelVersion(p.getModelVersion())
                .build();
    }
}
