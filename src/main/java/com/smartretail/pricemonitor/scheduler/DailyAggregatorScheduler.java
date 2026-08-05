package com.smartretail.pricemonitor.scheduler;

import com.smartretail.pricemonitor.constants.ReportStatus;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.PriceHistory;
import com.smartretail.pricemonitor.entity.PriceReport;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.PriceHistoryRepository;
import com.smartretail.pricemonitor.repository.PriceReportRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyAggregatorScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyAggregatorScheduler.class);

    private final PriceReportRepository priceReportRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;

    // Run every midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void aggregateDailyPrices() {
        log.info("Starting daily price aggregation...");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

        List<Commodity> commodities = commodityRepository.findAll();
        List<Market> markets = marketRepository.findAll();

        for (Commodity commodity : commodities) {
            for (Market market : markets) {
                // Find all verified reports for yesterday
                List<PriceReport> dailyReports = priceReportRepository.findByCommodityIdAndMarketIdAndStatus(
                        commodity.getId(), market.getId(), ReportStatus.VERIFIED
                ).stream()
                        .filter(r -> !r.getCreatedAt().isBefore(startOfDay) && !r.getCreatedAt().isAfter(endOfDay))
                        .toList();

                if (!dailyReports.isEmpty()) {
                    BigDecimal sum = dailyReports.stream()
                            .map(PriceReport::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avg = sum.divide(BigDecimal.valueOf(dailyReports.size()), 2, RoundingMode.HALF_UP);
                    
                    PriceHistory history = PriceHistory.builder()
                            .commodity(commodity)
                            .market(market)
                            .recordedDate(yesterday)
                            .averagePrice(avg)
                            .minPrice(dailyReports.stream().map(PriceReport::getPrice).min(BigDecimal::compareTo).orElse(avg))
                            .maxPrice(dailyReports.stream().map(PriceReport::getPrice).max(BigDecimal::compareTo).orElse(avg))
                            .source("AGGREGATED")
                            .build();

                    priceHistoryRepository.save(history);
                }
            }
        }
        log.info("Completed daily price aggregation.");
    }
}
