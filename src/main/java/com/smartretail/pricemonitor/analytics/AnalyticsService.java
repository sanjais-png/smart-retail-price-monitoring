package com.smartretail.pricemonitor.analytics;

import com.smartretail.pricemonitor.dto.AnalyticsSummaryResponse;
import com.smartretail.pricemonitor.entity.PriceHistory;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final PriceHistoryRepository priceHistoryRepository;
    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;

    /**
     * Compute comprehensive price analytics for a commodity across all markets.
     */
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getCommodityAnalytics(Long commodityId, LocalDate startDate, LocalDate endDate) {
        var commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", commodityId));

        if (startDate == null) startDate = LocalDate.now().minusMonths(3);
        if (endDate == null) endDate = LocalDate.now();

        List<BigDecimal> allPrices = new ArrayList<>();
        List<AnalyticsSummaryResponse.RegionComparisonDto> regionalPrices = new ArrayList<>();

        for (var market : marketRepository.findAll()) {
            List<PriceHistory> history = priceHistoryRepository
                    .findByCommodityIdAndMarketIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                            commodityId, market.getId(), startDate, endDate);

            if (!history.isEmpty()) {
                BigDecimal marketAvg = history.stream()
                        .map(PriceHistory::getAveragePrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);

                allPrices.addAll(history.stream().map(PriceHistory::getAveragePrice).collect(Collectors.toList()));
                regionalPrices.add(new AnalyticsSummaryResponse.RegionComparisonDto(
                        market.getName() + ", " + market.getCity(), marketAvg));
            }
        }

        if (allPrices.isEmpty()) {
            return AnalyticsSummaryResponse.builder()
                    .commodityName(commodity.getName())
                    .averagePrice(BigDecimal.ZERO)
                    .medianPrice(BigDecimal.ZERO)
                    .highestPrice(BigDecimal.ZERO)
                    .lowestPrice(BigDecimal.ZERO)
                    .regionalPrices(regionalPrices)
                    .build();
        }

        allPrices.sort(BigDecimal::compareTo);

        BigDecimal average = allPrices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allPrices.size()), 2, RoundingMode.HALF_UP);

        BigDecimal median = computeMedian(allPrices);
        BigDecimal highest = allPrices.get(allPrices.size() - 1);
        BigDecimal lowest = allPrices.get(0);

        return AnalyticsSummaryResponse.builder()
                .commodityName(commodity.getName())
                .averagePrice(average)
                .medianPrice(median)
                .highestPrice(highest)
                .lowestPrice(lowest)
                .regionalPrices(regionalPrices)
                .build();
    }

    private BigDecimal computeMedian(List<BigDecimal> sorted) {
        int n = sorted.size();
        if (n % 2 == 0) {
            return sorted.get(n / 2 - 1).add(sorted.get(n / 2))
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
        return sorted.get(n / 2);
    }
}
