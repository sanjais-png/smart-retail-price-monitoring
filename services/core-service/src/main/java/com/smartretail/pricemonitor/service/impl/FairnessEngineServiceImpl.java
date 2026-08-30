package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.constants.FairnessStatus;
import com.smartretail.pricemonitor.dto.FairnessCheckRequest;
import com.smartretail.pricemonitor.dto.FairnessCheckResponse;
import com.smartretail.pricemonitor.dto.MarketResponse;
import com.smartretail.pricemonitor.entity.*;
import com.smartretail.pricemonitor.exception.BadRequestException;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.*;
import com.smartretail.pricemonitor.service.FairnessEngineService;
import com.smartretail.pricemonitor.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartretail.pricemonitor.document.MarketPriceObservation;
import com.smartretail.pricemonitor.repository.mongo.MarketPriceObservationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FairnessEngineServiceImpl implements FairnessEngineService {

    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final UserRepository userRepository;
    private final PriceReportRepository priceReportRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final FairnessResultRepository fairnessResultRepository;
    private final LocationService locationService;
    private final MarketPriceObservationRepository observationRepository;

    @Override
    @Transactional
    public FairnessCheckResponse evaluateFairness(String username, FairnessCheckRequest request) {
        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        Commodity commodity = commodityRepository.findById(request.getCommodityId())
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", request.getCommodityId()));

        // ─── 1. Smart Purchase Location Resolution ───────────────────────────────
        Market market;
        if (request.getMarketId() != null && request.getMarketId() > 0) {
            market = marketRepository.findById(request.getMarketId())
                    .orElseThrow(() -> new ResourceNotFoundException("Market", "id", request.getMarketId()));
        } else if (request.getMarketName() != null && !request.getMarketName().isBlank()) {
            String city = request.getCity() != null ? request.getCity() : request.getMarketName();
            String state = request.getState() != null ? request.getState() : "Unknown State";
            MarketResponse resolved = locationService.findOrCreateMarketByLocation(
                    request.getMarketName(), city, state, request.getLatitude(), request.getLongitude()
            );
            market = marketRepository.findById(resolved.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Market", "id", resolved.getId()));
        } else if (request.getLatitude() != null && request.getLongitude() != null) {
            String marketName = "Auto GPS Market (" + request.getLatitude() + ", " + request.getLongitude() + ")";
            String city = request.getCity() != null ? request.getCity() : "Auto Detected City";
            String state = request.getState() != null ? request.getState() : "Auto Detected State";
            MarketResponse resolved = locationService.findOrCreateMarketByLocation(
                    marketName, city, state, request.getLatitude(), request.getLongitude()
            );
            market = marketRepository.findById(resolved.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Market", "id", resolved.getId()));
        } else {
            throw new BadRequestException(
                "Purchase location is required! Please provide one of: " +
                "(1) marketId, (2) marketName + city, or (3) latitude + longitude."
            );
        }

        BigDecimal purchasePrice = request.getPurchasePrice();

        // ─── 2. Production Inverse-Distance Weighted (IDW) Spatial Baseline Engine ─────
        SpatialBaselineResult baselineResult = resolveProductionBaseline(commodity, market);
        BigDecimal baselineMarketPrice = baselineResult.baselinePrice;
        double confidenceScore = baselineResult.confidenceScore;
        boolean isColdStart = baselineResult.isColdStart;
        String baselineSource = baselineResult.sourceDescription;

        log.info("Production Fairness Engine: Commodity='{}', Market='{}', City='{}', Baseline='₹{}' (ColdStart: {}, Confidence: {}%, Source: '{}')",
                commodity.getName(), market.getName(), market.getCity(), baselineMarketPrice, isColdStart, Math.round(confidenceScore * 100), baselineSource);

        // ─── 3. Compute Price Difference & Percentage ─────────────────────────────
        BigDecimal priceDiff = purchasePrice.subtract(baselineMarketPrice);
        double percentDiff = 0.0;
        if (baselineMarketPrice.compareTo(BigDecimal.ZERO) > 0) {
            percentDiff = priceDiff.divide(baselineMarketPrice, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        // ─── 4. Compute Fairness Score & Status ──────────────────────────────────
        FairnessStatus status;
        int fairnessScore;
        String recommendation;

        if (percentDiff < -5.0) {
            status = FairnessStatus.UNDERPRICED;
            fairnessScore = 100;
            recommendation = String.format(
                "Great deal! Your price (₹%.2f) is %.1f%% below the %s regional baseline (₹%.2f). [%s]",
                purchasePrice, Math.abs(percentDiff), market.getCity(), baselineMarketPrice, baselineSource);
        } else if (percentDiff <= 5.0) {
            status = FairnessStatus.FAIR;
            fairnessScore = Math.max(90, (int) Math.round(100 - (percentDiff * 0.8)));
            recommendation = String.format(
                "Fair Price! Your price (₹%.2f) is within acceptable market range for %s (Regional Baseline: ₹%.2f).",
                purchasePrice, market.getCity(), baselineMarketPrice);
        } else if (percentDiff <= 15.0) {
            status = FairnessStatus.SLIGHTLY_HIGH;
            fairnessScore = Math.max(70, (int) Math.round(90 - (percentDiff * 1.5)));
            recommendation = String.format(
                "Slightly High. You paid %.1f%% above regional average rate of ₹%.2f in %s. [%s]",
                percentDiff, baselineMarketPrice, market.getCity(), baselineSource);
        } else if (percentDiff <= 30.0) {
            status = FairnessStatus.HIGH;
            fairnessScore = Math.max(40, (int) Math.round(75 - (percentDiff * 1.5)));
            recommendation = String.format(
                "High Price Warning! Overcharged by %.1f%% compared to regional market rate (₹%.2f). [%s]",
                percentDiff, baselineMarketPrice, baselineSource);
        } else {
            status = FairnessStatus.VERY_HIGH;
            fairnessScore = Math.max(0, (int) Math.round(40 - (percentDiff * 0.5)));
            recommendation = String.format(
                "Potential Price Gouging Warning! You paid %.1f%% above regional baseline price (₹%.2f). Consider filing a complaint. [%s]",
                percentDiff, baselineMarketPrice, baselineSource);
        }

        if (isColdStart) {
            recommendation += " (Note: No active nearby market data found in your area yet. Initial benchmark used.)";
        }

        FairnessResult result = FairnessResult.builder()
                .user(user)
                .commodity(commodity)
                .market(market)
                .purchasePrice(purchasePrice)
                .marketPrice(baselineMarketPrice)
                .priceDifference(priceDiff)
                .percentageDifference(Math.round(percentDiff * 100.0) / 100.0)
                .fairnessScore(fairnessScore)
                .status(status)
                .recommendation(recommendation)
                .confidenceScore(confidenceScore)
                .isColdStart(isColdStart)
                .baselineSource(baselineSource)
                .referenceSource(baselineResult.referenceSource)
                .referenceObservedAt(baselineResult.referenceObservedAt)
                .referenceObservationId(baselineResult.referenceObservationId)
                .build();

        FairnessResult saved = fairnessResultRepository.save(result);

        return FairnessCheckResponse.builder()
                .id(saved.getId())
                .commodityId(commodity.getId())
                .commodityName(commodity.getName())
                .marketId(market.getId())
                .marketName(market.getName())
                .purchasePrice(purchasePrice)
                .marketPrice(baselineMarketPrice)
                .priceDifference(priceDiff)
                .percentageDifference(Math.round(percentDiff * 100.0) / 100.0)
                .fairnessScore(fairnessScore)
                .status(status)
                .recommendation(recommendation)
                .confidenceScore(confidenceScore)
                .isColdStart(isColdStart)
                .baselineSource(baselineSource)
                .calculatedAt(saved.getCalculatedAt())
                .build();
    }

    /**
     * Production-grade spatial interpolation using Haversine Geolocation Distance-Decay (IDW).
     */
    private SpatialBaselineResult resolveProductionBaseline(Commodity commodity, Market targetMarket) {
        // Tier 0: Primary MongoDB High-Volume Market Observation Store
        try {
            if (observationRepository != null) {
                Optional<MarketPriceObservation> mongoObs = observationRepository.findTopByCommodityIdAndMarketIdOrderByObservedAtDesc(commodity.getId(), targetMarket.getId());
                if (mongoObs.isPresent() && mongoObs.get().getModalPrice() != null) {
                    MarketPriceObservation obs = mongoObs.get();
                    SpatialBaselineResult res = new SpatialBaselineResult(
                            obs.getModalPrice(), 0.98, false, "MongoDB Live Agmarknet Observation (" + obs.getMarketName() + ")"
                    );
                    res.referenceSource = obs.getSource() != null ? obs.getSource() : "AGMARKNET";
                    res.referenceObservedAt = obs.getObservedAt() != null ? obs.getObservedAt() : Instant.now();
                    res.referenceObservationId = obs.getId();
                    return res;
                }
            }
        } catch (Exception e) {
            log.warn("MongoDB observation lookup exception: {}. Falling back to cached benchmark.", e.getMessage());
        }

        // Tier 1: Verified reports in THIS exact market
        Optional<BigDecimal> verifiedAvg = priceReportRepository.findAverageVerifiedPrice(commodity.getId(), targetMarket.getId());
        if (verifiedAvg.isPresent()) {
            SpatialBaselineResult res = new SpatialBaselineResult(verifiedAvg.get(), 0.95, false, "Direct Market Verified Reports [CACHE_FALLBACK]");
            res.referenceSource = "CACHE_FALLBACK";
            return res;
        }

        // Tier 2: Historical price records in THIS exact market
        Optional<PriceHistory> latestHistory = priceHistoryRepository.findLatestPrice(commodity.getId(), targetMarket.getId());
        if (latestHistory.isPresent()) {
            SpatialBaselineResult res = new SpatialBaselineResult(latestHistory.get().getAveragePrice(), 0.85, false, "Direct Market Price History [CACHE_FALLBACK]");
            res.referenceSource = "CACHE_FALLBACK";
            return res;
        }

        // Tier 3: Verified reports in the SAME CITY / DISTRICT
        Optional<BigDecimal> cityAvg = priceReportRepository.findAverageVerifiedPriceByCity(commodity.getId(), targetMarket.getCity());
        if (cityAvg.isPresent()) {
            return new SpatialBaselineResult(cityAvg.get(), 0.82, false, targetMarket.getCity() + " District Verified Average");
        }

        // Tier 4: Inverse-Distance Weighted (IDW) Spatial Proximity Engine (Nearby Markets within 300 km)
        double targetLat = targetMarket.getLatitude() != null ? targetMarket.getLatitude() : 11.0168; // Default Coimbatore lat
        double targetLon = targetMarket.getLongitude() != null ? targetMarket.getLongitude() : 76.9558; // Default Coimbatore lon

        List<Market> allMarkets = marketRepository.findAll();
        double totalWeightedPriceSum = 0.0;
        double totalWeightSum = 0.0;
        double closestDistanceKm = Double.MAX_VALUE;
        int nearbyMarketsCount = 0;

        for (Market otherMarket : allMarkets) {
            if (otherMarket.getId().equals(targetMarket.getId())) continue;
            if (otherMarket.getLatitude() == null || otherMarket.getLongitude() == null) continue;

            Optional<BigDecimal> otherMarketPriceOpt = priceReportRepository.findAverageVerifiedPrice(commodity.getId(), otherMarket.getId());
            if (otherMarketPriceOpt.isEmpty()) {
                Optional<PriceHistory> otherHistory = priceHistoryRepository.findLatestPrice(commodity.getId(), otherMarket.getId());
                if (otherHistory.isPresent()) {
                    otherMarketPriceOpt = Optional.of(otherHistory.get().getAveragePrice());
                }
            }

            if (otherMarketPriceOpt.isPresent()) {
                double price = otherMarketPriceOpt.get().doubleValue();
                double distKm = calculateHaversineDistanceKm(targetLat, targetLon, otherMarket.getLatitude(), otherMarket.getLongitude());

                if (distKm < closestDistanceKm) {
                    closestDistanceKm = distKm;
                }

                // Consider only nearby markets within 350 km radius (Regional Buffer)
                if (distKm <= 350.0) {
                    double weight = 1.0 / Math.pow(distKm + 10.0, 2);
                    totalWeightedPriceSum += price * weight;
                    totalWeightSum += weight;
                    nearbyMarketsCount++;
                }
            }
        }

        if (totalWeightSum > 0.0 && nearbyMarketsCount > 0) {
            BigDecimal idwPrice = BigDecimal.valueOf(totalWeightedPriceSum / totalWeightSum).setScale(2, RoundingMode.HALF_UP);
            double confidence = closestDistanceKm < 50.0 ? 0.78 : (closestDistanceKm < 150.0 ? 0.70 : 0.60);
            String desc = String.format("Spatial IDW Interpolation (%d nearby markets, closest %.0f km)", nearbyMarketsCount, closestDistanceKm);
            return new SpatialBaselineResult(idwPrice, confidence, false, desc);
        }

        // Tier 5: Absolute Cold Start (No data anywhere in regional vicinity)
        BigDecimal basePrice = commodity.getBaseBenchmarkPrice() != null ? commodity.getBaseBenchmarkPrice() : BigDecimal.valueOf(35.00);
        return new SpatialBaselineResult(
            basePrice,
            0.35,
            true, // Explicit Cold Start Flag
            "Government Mandi Benchmark (Cold Start - No regional data)"
        );
    }

    private double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private static class SpatialBaselineResult {
        final BigDecimal baselinePrice;
        final double confidenceScore;
        final boolean isColdStart;
        final String sourceDescription;
        String referenceSource = "AGMARKNET";
        Instant referenceObservedAt = Instant.now();
        String referenceObservationId;

        SpatialBaselineResult(BigDecimal baselinePrice, double confidenceScore, boolean isColdStart, String sourceDescription) {
            this.baselinePrice = baselinePrice;
            this.confidenceScore = confidenceScore;
            this.isColdStart = isColdStart;
            this.sourceDescription = sourceDescription;
        }
    }
}
