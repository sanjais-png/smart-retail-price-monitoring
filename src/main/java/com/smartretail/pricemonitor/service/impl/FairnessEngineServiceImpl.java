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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

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

    @Override
    @Transactional
    public FairnessCheckResponse evaluateFairness(String username, FairnessCheckRequest request) {
        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        Commodity commodity = commodityRepository.findById(request.getCommodityId())
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", request.getCommodityId()));

        // ─── Smart Purchase Location Resolution ───────────────────────────────────
        // Priority 1: marketId provided directly (user selected from dropdown or search)
        // Priority 2: marketName + city + state provided (user typed the purchase city)
        // Priority 3: latitude + longitude provided (GPS from purchase location)
        Market market;
        String locationResolutionMethod;

        if (request.getMarketId() != null && request.getMarketId() > 0) {
            // Option A: User selected market directly by ID
            market = marketRepository.findById(request.getMarketId())
                    .orElseThrow(() -> new ResourceNotFoundException("Market", "id", request.getMarketId()));
            locationResolutionMethod = "Direct Market ID Selection";

        } else if (request.getMarketName() != null && !request.getMarketName().isBlank()) {
            // Option B: User typed purchase market name / city (e.g. "Koyambedu, Chennai")
            String city = request.getCity() != null ? request.getCity() : request.getMarketName();
            String state = request.getState() != null ? request.getState() : "Unknown State";
            MarketResponse resolved = locationService.findOrCreateMarketByLocation(
                    request.getMarketName(), city, state, request.getLatitude(), request.getLongitude()
            );
            market = marketRepository.findById(resolved.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Market", "id", resolved.getId()));
            locationResolutionMethod = "Purchase City / Market Name Lookup";

        } else if (request.getLatitude() != null && request.getLongitude() != null) {
            // Option C: GPS coordinates provided (auto-resolved from mobile device)
            String marketName = "Auto GPS Market (" + request.getLatitude() + ", " + request.getLongitude() + ")";
            String city = request.getCity() != null ? request.getCity() : "Auto Detected City";
            String state = request.getState() != null ? request.getState() : "Auto Detected State";
            MarketResponse resolved = locationService.findOrCreateMarketByLocation(
                    marketName, city, state, request.getLatitude(), request.getLongitude()
            );
            market = marketRepository.findById(resolved.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Market", "id", resolved.getId()));
            locationResolutionMethod = "GPS Coordinate Auto-Resolution";

        } else {
            throw new BadRequestException(
                "Purchase location is required! Please provide one of: " +
                "(1) marketId — select from market search, " +
                "(2) marketName + city + state — type where you bought it, or " +
                "(3) latitude + longitude — from your GPS device."
            );
        }

        log.info("Fairness check: Commodity='{}', Market='{}', City='{}', ResolutionMethod='{}'",
                commodity.getName(), market.getName(), market.getCity(), locationResolutionMethod);

        BigDecimal purchasePrice = request.getPurchasePrice();

        // ─── Determine Baseline Market Price ──────────────────────────────────────
        BigDecimal baselineMarketPrice;
        double confidenceScore = 0.90;

        Optional<BigDecimal> verifiedAvg = priceReportRepository.findAverageVerifiedPrice(commodity.getId(), market.getId());
        if (verifiedAvg.isPresent()) {
            baselineMarketPrice = verifiedAvg.get();
            confidenceScore = 0.95;
        } else {
            Optional<PriceHistory> latestHistory = priceHistoryRepository.findLatestPrice(commodity.getId(), market.getId());
            if (latestHistory.isPresent()) {
                baselineMarketPrice = latestHistory.get().getAveragePrice();
                confidenceScore = 0.85;
            } else {
                baselineMarketPrice = purchasePrice;
                confidenceScore = 0.50;
            }
        }

        // ─── Compute Difference & Percentage ──────────────────────────────────────
        BigDecimal priceDiff = purchasePrice.subtract(baselineMarketPrice);
        double percentDiff = 0.0;
        if (baselineMarketPrice.compareTo(BigDecimal.ZERO) > 0) {
            percentDiff = priceDiff.divide(baselineMarketPrice, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        // ─── Compute Fairness Score & Status ──────────────────────────────────────
        FairnessStatus status;
        int fairnessScore;
        String recommendation;

        if (percentDiff < -5.0) {
            status = FairnessStatus.UNDERPRICED;
            fairnessScore = 100;
            recommendation = String.format(
                "Great deal! Your purchase price (₹%.2f) is %.1f%% below the %s market average (₹%.2f).",
                purchasePrice, Math.abs(percentDiff), market.getCity(), baselineMarketPrice);
        } else if (percentDiff <= 5.0) {
            status = FairnessStatus.FAIR;
            fairnessScore = Math.max(90, (int) Math.round(100 - (percentDiff * 0.8)));
            recommendation = String.format(
                "Fair Price! Your price (₹%.2f) is within acceptable range for %s market (Baseline: ₹%.2f).",
                purchasePrice, market.getCity(), baselineMarketPrice);
        } else if (percentDiff <= 15.0) {
            status = FairnessStatus.SLIGHTLY_HIGH;
            fairnessScore = Math.max(70, (int) Math.round(90 - (percentDiff * 1.5)));
            recommendation = String.format(
                "Slightly High. You paid %.1f%% above average %s market rate of ₹%.2f.",
                percentDiff, market.getCity(), baselineMarketPrice);
        } else if (percentDiff <= 30.0) {
            status = FairnessStatus.HIGH;
            fairnessScore = Math.max(40, (int) Math.round(75 - (percentDiff * 1.5)));
            recommendation = String.format(
                "High Price Warning. Overcharged by %.1f%% compared to %s local market rate (₹%.2f).",
                percentDiff, market.getCity(), baselineMarketPrice);
        } else {
            status = FairnessStatus.VERY_HIGH;
            fairnessScore = Math.max(0, (int) Math.round(40 - (percentDiff * 0.5)));
            recommendation = String.format(
                "Potential Price Gouging in %s! You paid %.1f%% above market average (₹%.2f). Consider filing a complaint.",
                market.getCity(), percentDiff, baselineMarketPrice);
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
                .calculatedAt(saved.getCalculatedAt())
                .build();
    }
}
