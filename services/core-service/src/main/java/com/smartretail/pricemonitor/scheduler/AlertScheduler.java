package com.smartretail.pricemonitor.scheduler;

import com.smartretail.pricemonitor.constants.NotificationType;
import com.smartretail.pricemonitor.document.MarketPriceObservation;
import com.smartretail.pricemonitor.entity.Alert;
import com.smartretail.pricemonitor.entity.PriceHistory;
import com.smartretail.pricemonitor.repository.AlertRepository;
import com.smartretail.pricemonitor.repository.PriceHistoryRepository;
import com.smartretail.pricemonitor.repository.mongo.MarketPriceObservationRepository;
import com.smartretail.pricemonitor.service.AlertEvaluationEngine;
import com.smartretail.pricemonitor.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);

    private final AlertRepository alertRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final NotificationService notificationService;
    
    @Autowired(required = false)
    private MarketPriceObservationRepository mongoObservationRepository;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processPriceAlerts() {
        log.info("Starting scheduled price alert evaluation...");

        List<Alert> activeAlerts = alertRepository.findByIsActiveTrue();
        if (activeAlerts.isEmpty()) {
            log.info("No active price alerts found to evaluate.");
            return;
        }

        for (Alert alert : activeAlerts) {
            try {
                evaluateSingleAlert(alert);
            } catch (Exception e) {
                log.error("Failed to process alert ID: {}", alert.getId(), e);
            }
        }

        log.info("Completed scheduled price alert processing for {} alerts.", activeAlerts.size());
    }

    @Transactional
    public void evaluateSingleAlert(Alert alert) {
        if (alert == null || !alert.isActive()) return;

        Long commodityId = alert.getCommodity().getId();
        Long marketId = alert.getMarket().getId();
        
        BigDecimal observedPrice = null;
        String observedUnit = alert.getCommodity().getUnit();

        // 1. Check latest JPA PriceHistory
        Optional<PriceHistory> latestHistory = priceHistoryRepository.findLatestPrice(commodityId, marketId);
        if (latestHistory.isPresent()) {
            observedPrice = latestHistory.get().getAveragePrice();
        } else if (mongoObservationRepository != null) {
            // 2. Fallback to Mongo MarketPriceObservation
            Optional<MarketPriceObservation> obs = mongoObservationRepository
                    .findTopByCommodityIdAndMarketIdOrderByObservedAtDesc(commodityId, marketId);
            if (obs.isPresent()) {
                observedPrice = obs.get().getModalPrice();
                if (obs.get().getUnit() != null) {
                    observedUnit = obs.get().getUnit();
                }
            }
        }

        if (observedPrice == null) {
            log.debug("No observed price found for alert ID {} (Commodity {}, Market {}). Skipping evaluation.",
                    alert.getId(), alert.getCommodity().getName(), alert.getMarket().getName());
            return;
        }

        AlertEvaluationEngine.EvaluationResult result = AlertEvaluationEngine.evaluate(alert, observedPrice, observedUnit);
        
        LocalDateTime now = LocalDateTime.now();
        alert.setLastEvaluatedPrice(result.getNormalizedPrice() != null ? result.getNormalizedPrice() : observedPrice);
        alert.setLastEvaluatedAt(now);

        if (result.isTriggered()) {
            alert.setLastTriggeredPrice(result.getNormalizedPrice() != null ? result.getNormalizedPrice() : observedPrice);
            alert.setLastTriggeredAt(now);
            
            notificationService.sendNotification(
                    alert.getUser().getId(),
                    "Price Alert Triggered!",
                    result.getMessage(),
                    NotificationType.SYSTEM
            );
            log.info("Alert ID {} triggered for user {} (Commodity: {}). Message: {}",
                    alert.getId(), alert.getUser().getUsername(), alert.getCommodity().getName(), result.getMessage());
        }

        alertRepository.save(alert);
    }
}
