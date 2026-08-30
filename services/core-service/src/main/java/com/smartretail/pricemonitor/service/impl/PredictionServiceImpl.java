package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.document.MarketPriceObservation;
import com.smartretail.pricemonitor.dto.PredictionRequest;
import com.smartretail.pricemonitor.dto.PredictionResponse;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.Prediction;
import com.smartretail.pricemonitor.entity.PriceHistory;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.prediction.PriceDataPreprocessor;
import com.smartretail.pricemonitor.prediction.PricePredictionEngine;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.PredictionRepository;
import com.smartretail.pricemonitor.repository.PriceHistoryRepository;
import com.smartretail.pricemonitor.repository.mongo.MarketPriceObservationRepository;
import com.smartretail.pricemonitor.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionServiceImpl implements PredictionService {

    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PredictionRepository predictionRepository;
    private final PricePredictionEngine predictionEngine;
    private final PriceDataPreprocessor dataPreprocessor;
    private final MarketPriceObservationRepository observationRepository;

    @Override
    @Transactional
    public PredictionResponse generatePrediction(PredictionRequest request) {
        Commodity commodity = commodityRepository.findById(request.getCommodityId())
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", request.getCommodityId()));

        Market market = marketRepository.findById(request.getMarketId())
                .orElseThrow(() -> new ResourceNotFoundException("Market", "id", request.getMarketId()));

        // 1. Fetch MySQL Historical Observations
        List<PriceHistory> mysqlHistory = priceHistoryRepository.findByCommodityIdAndMarketIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                commodity.getId(), market.getId(), LocalDate.now().minusDays(180), LocalDate.now()
        );

        // 2. Fetch MongoDB Time-Series Observations
        List<MarketPriceObservation> mongoObs = new ArrayList<>();
        try {
            if (observationRepository != null) {
                Instant end = Instant.now();
                Instant start = end.minus(180, ChronoUnit.DAYS);
                mongoObs = observationRepository
                        .findByCommodityIdAndMarketIdAndObservedAtBetween(commodity.getId(), market.getId(), start, end, PageRequest.of(0, 500))
                        .getContent();
            }
        } catch (Exception e) {
            log.warn("Could not query MongoDB observations for predictions: {}", e.getMessage());
        }

        // 3. Data Pipeline: Deduplicate, Sort, Normalize & Clean Outliers
        List<PriceDataPreprocessor.NormalizedObservation> cleanObservations = dataPreprocessor.preprocess(mysqlHistory, mongoObs);

        // 4. Run ML Forecasting Engine with Walk-Forward Validation & Model Selection
        PricePredictionEngine.DetailedPredictionResult detailedResult = predictionEngine.predict(cleanObservations, request.getTimeframe());

        // 5. Persist Prediction to MySQL
        Prediction prediction = predictionRepository.findByCommodityIdAndMarketIdAndTimeframe(
                commodity.getId(), market.getId(), request.getTimeframe()
        ).orElse(Prediction.builder()
                .commodity(commodity)
                .market(market)
                .timeframe(request.getTimeframe())
                .build());

        prediction.setPredictedPrice(detailedResult.predictedPrice());
        prediction.setConfidenceScore(detailedResult.evaluation().mae() < 5.0 ? 0.85 : 0.70);
        prediction.setTargetDate(detailedResult.targetDate());
        prediction.setModelVersion(detailedResult.modelVersion());

        Prediction saved = predictionRepository.save(prediction);

        return mapToDetailedResponse(saved, detailedResult);
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

    private PredictionResponse mapToDetailedResponse(Prediction p, PricePredictionEngine.DetailedPredictionResult detailed) {
        return PredictionResponse.builder()
                .id(p.getId())
                .commodityId(p.getCommodity().getId())
                .commodityName(p.getCommodity().getName())
                .marketId(p.getMarket().getId())
                .marketName(p.getMarket().getName())
                .targetDate(p.getTargetDate())
                .timeframe(p.getTimeframe())
                .predictedPrice(detailed.predictedPrice())
                .lowerBound(detailed.lowerBound())
                .upperBound(detailed.upperBound())
                .intervalType(detailed.intervalType())
                .confidenceScore(p.getConfidenceScore())
                .modelVersion(detailed.modelVersion())
                .build();
    }
}
