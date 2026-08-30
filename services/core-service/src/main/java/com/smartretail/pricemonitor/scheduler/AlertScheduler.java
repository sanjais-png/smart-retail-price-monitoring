package com.smartretail.pricemonitor.scheduler;

import com.smartretail.pricemonitor.constants.NotificationType;
import com.smartretail.pricemonitor.entity.Alert;
import com.smartretail.pricemonitor.entity.PriceHistory;
import com.smartretail.pricemonitor.repository.AlertRepository;
import com.smartretail.pricemonitor.repository.PriceHistoryRepository;
import com.smartretail.pricemonitor.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);

    private final AlertRepository alertRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final NotificationService notificationService;

    // Run every day at 8 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void processPriceAlerts() {
        log.info("Starting scheduled price alert processing...");

        List<Alert> activeAlerts = alertRepository.findByIsActiveTrue();
        LocalDate today = LocalDate.now();

        for (Alert alert : activeAlerts) {
            try {
                // Get today's price history for the specific commodity and market
                List<PriceHistory> todayHistory = priceHistoryRepository
                        .findByCommodityIdAndMarketIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                                alert.getCommodity().getId(), alert.getMarket().getId(), today, today);

                if (!todayHistory.isEmpty()) {
                    // For simplicity, take the first one or average
                    PriceHistory latest = todayHistory.get(todayHistory.size() - 1);
                    boolean trigger = false;
                    String message = "";

                    switch (alert.getAlertType()) {
                        case PRICE_DECREASE:
                            if (latest.getAveragePrice().compareTo(alert.getTargetPrice()) <= 0) {
                                trigger = true;
                                message = String.format("Price drop alert: %s at %s has dropped to %s",
                                        alert.getCommodity().getName(), alert.getMarket().getName(), latest.getAveragePrice());
                            }
                            break;
                        case PRICE_INCREASE:
                            if (latest.getAveragePrice().compareTo(alert.getTargetPrice()) >= 0) {
                                trigger = true;
                                message = String.format("Price increase alert: %s at %s has risen to %s",
                                        alert.getCommodity().getName(), alert.getMarket().getName(), latest.getAveragePrice());
                            }
                            break;
                        case THRESHOLD_CROSS:
                            // Handle logic if we had inventory tracking or cross logic
                            break;
                    }

                    if (trigger) {
                        notificationService.sendNotification(
                                alert.getUser().getId(),
                                "Price Alert Triggered!",
                                message,
                                NotificationType.SYSTEM
                        );
                        log.info("Alert triggered for user {} for commodity {}", alert.getUser().getUsername(), alert.getCommodity().getName());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process alert ID: {}", alert.getId(), e);
            }
        }

        log.info("Completed scheduled price alert processing.");
    }
}
