package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.AnomalyAnalysisResponse;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.PriceReport;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.PriceReportRepository;
import com.smartretail.pricemonitor.service.PriceAnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.smartretail.pricemonitor.document.MarketPriceObservation;
import com.smartretail.pricemonitor.repository.mongo.MarketPriceObservationRepository;
import org.springframework.data.domain.PageRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAnomalyDetectionServiceImpl implements PriceAnomalyDetectionService {

    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final PriceReportRepository priceReportRepository;
    private final MarketPriceObservationRepository observationRepository;

    @Override
    public AnomalyAnalysisResponse detectAnomaly(Long commodityId, Long marketId, BigDecimal targetPrice) {
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", commodityId));

        Market market = marketRepository.findById(marketId)
                .orElseThrow(() -> new ResourceNotFoundException("Market", "id", marketId));

        double mean = targetPrice.doubleValue();
        double stdDev = 2.50; // default baseline dispersion
        boolean usedMongoObservations = false;

        // Query MongoDB for 30-day historical market observation snapshots
        try {
            if (observationRepository != null) {
                Instant end = Instant.now();
                Instant start = end.minus(30, ChronoUnit.DAYS);
                List<MarketPriceObservation> obsList = observationRepository
                        .findByCommodityIdAndMarketIdAndObservedAtBetween(commodityId, marketId, start, end, PageRequest.of(0, 100))
                        .getContent();

                if (!obsList.isEmpty()) {
                    double sum = obsList.stream().mapToDouble(o -> o.getModalPrice() != null ? o.getModalPrice().doubleValue() : targetPrice.doubleValue()).sum();
                    mean = sum / obsList.size();

                    double temp = 0.0;
                    for (MarketPriceObservation o : obsList) {
                        double p = o.getModalPrice() != null ? o.getModalPrice().doubleValue() : mean;
                        double diff = p - mean;
                        temp += diff * diff;
                    }
                    if (obsList.size() > 1) {
                        stdDev = Math.sqrt(temp / (obsList.size() - 1));
                    }
                    usedMongoObservations = true;
                }
            }
        } catch (Exception e) {
            log.warn("MongoDB anomaly observation query exception: {}", e.getMessage());
        }

        if (!usedMongoObservations) {
            List<PriceReport> reports = priceReportRepository.findByCommodityIdAndMarketId(commodityId, marketId);
            if (!reports.isEmpty()) {
                double sum = 0.0;
                for (PriceReport r : reports) {
                    sum += r.getPrice().doubleValue();
                }
                mean = sum / reports.size();

                double temp = 0.0;
                for (PriceReport r : reports) {
                    double diff = r.getPrice().doubleValue() - mean;
                    temp += diff * diff;
                }
                if (reports.size() > 1) {
                    stdDev = Math.sqrt(temp / (reports.size() - 1));
                }
            }
        }

        if (stdDev == 0.0) stdDev = 1.0;

        double zScore = (targetPrice.doubleValue() - mean) / stdDev;
        boolean isAnomalous = Math.abs(zScore) >= 2.5; // >2.5 standard deviations is a statistical outlier
        boolean isCollusionRisk = zScore >= 3.0;

        String riskLevel = "NORMAL";
        String explanation = "Price is within normal statistical distribution.";

        if (zScore >= 3.0) {
            riskLevel = "CRITICAL_GOUGING";
            explanation = String.format("Extreme Anomaly! Z-Score is %.2f (+%.1f SDs above market mean ₹%.2f). Suspicious price gouging detected.", zScore, zScore, mean);
        } else if (zScore >= 2.0) {
            riskLevel = "HIGH_ANOMALY";
            explanation = String.format("Statistical Anomaly Detected. Z-Score is %.2f above mean (₹%.2f). Requires inspection.", zScore, mean);
        } else if (zScore <= -2.0) {
            riskLevel = "LOW_OUTLIER";
            explanation = String.format("Underpriced Outlier. Z-Score is %.2f below market mean (₹%.2f).", zScore, mean);
        }

        return AnomalyAnalysisResponse.builder()
                .commodityId(commodity.getId())
                .commodityName(commodity.getName())
                .marketId(market.getId())
                .marketName(market.getName())
                .evaluatedPrice(targetPrice)
                .marketMeanPrice(BigDecimal.valueOf(mean).setScale(2, RoundingMode.HALF_UP))
                .standardDeviation(BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP))
                .zScore(Math.round(zScore * 100.0) / 100.0)
                .isAnomalous(isAnomalous)
                .isCollusionRisk(isCollusionRisk)
                .riskLevel(riskLevel)
                .explanation(explanation)
                .build();
    }
}
