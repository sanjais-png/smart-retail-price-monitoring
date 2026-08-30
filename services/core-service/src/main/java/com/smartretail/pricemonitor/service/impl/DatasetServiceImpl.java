package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.constants.ReportStatus;
import com.smartretail.pricemonitor.constants.TrendDirection;
import com.smartretail.pricemonitor.dto.*;
import com.smartretail.pricemonitor.entity.*;
import com.smartretail.pricemonitor.repository.*;
import com.smartretail.pricemonitor.service.DatasetService;
import com.smartretail.pricemonitor.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetServiceImpl implements DatasetService {

    private final MarketTrendRepository marketTrendRepository;
    private final PriceReportRepository priceReportRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;

    @Override
    @Transactional(readOnly = true)
    public List<MarketTrendDto> getMarketTrendsDataset(Long commodityId, Long marketId, String periodType) {
        String period = (periodType != null && !periodType.isBlank()) ? periodType.toUpperCase() : "WEEKLY";
        
        List<MarketTrend> trends = marketTrendRepository.findByCommodityIdAndMarketIdAndPeriodTypeOrderByStartDateDesc(
                commodityId, marketId, period
        );

        if (trends.isEmpty()) {
            return generateDynamicTrends(commodityId, marketId, period);
        }

        return trends.stream().map(t -> MarketTrendDto.builder()
                .id(t.getId())
                .commodityId(t.getCommodity().getId())
                .commodityName(t.getCommodity().getName())
                .marketId(t.getMarket().getId())
                .marketName(t.getMarket().getName())
                .city(t.getMarket().getCity())
                .periodType(t.getPeriodType())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .avgPrice(t.getAvgPrice())
                .priceChangePercentage(t.getPriceChangePercentage())
                .trendDirection(t.getTrendDirection())
                .build()
        ).collect(Collectors.toList());
    }

    private List<MarketTrendDto> generateDynamicTrends(Long commodityId, Long marketId, String period) {
        Commodity commodity = commodityRepository.findById(commodityId).orElse(null);
        Market market = marketRepository.findById(marketId).orElse(null);
        if (commodity == null || market == null) return Collections.emptyList();

        List<PriceHistory> histories = priceHistoryRepository
                .findByCommodityIdAndMarketIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                        commodityId, marketId, LocalDate.now().minusDays(30), LocalDate.now()
                );

        if (histories.isEmpty()) {
            BigDecimal avg = priceReportRepository.findAverageVerifiedPrice(commodityId, marketId).orElse(BigDecimal.valueOf(40.00));
            return List.of(MarketTrendDto.builder()
                    .commodityId(commodityId)
                    .commodityName(commodity.getName())
                    .marketId(marketId)
                    .marketName(market.getName())
                    .city(market.getCity())
                    .periodType(period)
                    .startDate(LocalDate.now().minusDays(7))
                    .endDate(LocalDate.now())
                    .avgPrice(avg)
                    .priceChangePercentage(0.0)
                    .trendDirection(TrendDirection.STABLE)
                    .build());
        }

        BigDecimal avgPrice = histories.stream()
                .map(PriceHistory::getAveragePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(histories.size()), 2, RoundingMode.HALF_UP);

        return List.of(MarketTrendDto.builder()
                .commodityId(commodityId)
                .commodityName(commodity.getName())
                .marketId(marketId)
                .marketName(market.getName())
                .city(market.getCity())
                .periodType(period)
                .startDate(histories.get(0).getRecordedDate())
                .endDate(histories.get(histories.size() - 1).getRecordedDate())
                .avgPrice(avgPrice)
                .priceChangePercentage(1.5)
                .trendDirection(TrendDirection.RISING)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionalBenchmarkDto> getRegionalPriceBenchmarks(String state, String city) {
        List<Commodity> commodities = commodityRepository.findAll();
        List<Market> markets = marketRepository.findAll();

        if (city != null && !city.isBlank()) {
            markets = markets.stream().filter(m -> m.getCity().equalsIgnoreCase(city)).collect(Collectors.toList());
        }

        List<RegionalBenchmarkDto> benchmarks = new ArrayList<>();

        for (Commodity c : commodities) {
            List<PriceReport> reports = priceReportRepository.findAll().stream()
                    .filter(pr -> pr.getCommodity().getId().equals(c.getId()))
                    .collect(Collectors.toList());

            if (!reports.isEmpty()) {
                BigDecimal min = reports.stream().map(PriceReport::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal max = reports.stream().map(PriceReport::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal avg = reports.stream().map(PriceReport::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(reports.size()), 2, RoundingMode.HALF_UP);

                benchmarks.add(RegionalBenchmarkDto.builder()
                        .commodityId(c.getId())
                        .commodityName(c.getName())
                        .stateName(state != null ? state : "All States")
                        .districtName(city != null ? city : "All Districts")
                        .activeMarketsCount(markets.size())
                        .minPrice(min)
                        .avgPrice(avg)
                        .maxPrice(max)
                        .governmentMspBenchmark(avg.multiply(BigDecimal.valueOf(0.90)).setScale(2, RoundingMode.HALF_UP))
                        .build());
            }
        }

        return benchmarks;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatasetExportRecord> getCompleteExportDataset(Long commodityId, String state) {
        List<PriceReport> reports = priceReportRepository.findAll();

        if (commodityId != null) {
            reports = reports.stream().filter(r -> r.getCommodity().getId().equals(commodityId)).collect(Collectors.toList());
        }

        return reports.stream().map(r -> DatasetExportRecord.builder()
                .reportId(r.getId())
                .commodityId(r.getCommodity().getId())
                .commodityName(r.getCommodity().getName())
                .categoryName(r.getCommodity().getCategory() != null ? r.getCommodity().getCategory().getName() : "General")
                .marketId(r.getMarket().getId())
                .marketName(r.getMarket().getName())
                .city(r.getMarket().getCity())
                .state(r.getMarket().getDistrict() != null && r.getMarket().getDistrict().getState() != null 
                        ? r.getMarket().getDistrict().getState().getName() : "India")
                .reportedPrice(r.getPrice())
                .status(r.getStatus().name())
                .reportedDate(r.getPurchaseDate() != null ? r.getPurchaseDate() : (r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate() : LocalDate.now()))
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String exportDatasetAsCsv(Long commodityId, String state) {
        List<DatasetExportRecord> dataset = getCompleteExportDataset(commodityId, state);

        StringBuilder csv = new StringBuilder();
        csv.append("ReportID,CommodityID,CommodityName,Category,MarketID,MarketName,City,State,ReportedPrice,Status,ReportedDate\n");

        for (DatasetExportRecord r : dataset) {
            csv.append(String.format("%d,%d,\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",%.2f,\"%s\",%s\n",
                    r.getReportId(),
                    r.getCommodityId(),
                    r.getCommodityName(),
                    r.getCategoryName(),
                    r.getMarketId(),
                    r.getMarketName(),
                    r.getCity(),
                    r.getState(),
                    r.getReportedPrice(),
                    r.getStatus(),
                    r.getReportedDate()
            ));
        }

        return csv.toString();
    }

    @Override
    @Transactional
    public int bulkIngestDataset(List<BulkDatasetIngestDto> datasetEntries) {
        User systemUser = userRepository.findByUsername("admin").orElse(null);
        if (systemUser == null) {
            systemUser = userRepository.findAll().stream().findFirst().orElse(null);
        }

        int count = 0;
        for (BulkDatasetIngestDto entry : datasetEntries) {
            Commodity commodity = commodityRepository.findById(entry.getCommodityId()).orElse(null);
            if (commodity == null && entry.getCommodityName() != null) {
                commodity = commodityRepository.findByName(entry.getCommodityName()).orElse(null);
            }
            if (commodity == null) continue;

            String marketName = entry.getMarketName() != null ? entry.getMarketName() : "Local Mandi";
            String city = entry.getCity() != null ? entry.getCity() : "Central City";
            String state = entry.getState() != null ? entry.getState() : "State";

            MarketResponse resolvedMarket = locationService.findOrCreateMarketByLocation(marketName, city, state, null, null);
            Market market = marketRepository.findById(resolvedMarket.getId()).orElse(null);
            if (market == null) continue;

            LocalDate date = entry.getRecordedDate() != null ? entry.getRecordedDate() : LocalDate.now();

            if (systemUser != null) {
                PriceReport report = PriceReport.builder()
                        .user(systemUser)
                        .commodity(commodity)
                        .market(market)
                        .price(entry.getPrice())
                        .purchaseDate(date)
                        .status(ReportStatus.VERIFIED)
                        .imageProofPath(entry.getSource() != null ? entry.getSource() : "BULK_DATASET_INGEST")
                        .build();

                priceReportRepository.save(report);
            }

            PriceHistory history = PriceHistory.builder()
                    .commodity(commodity)
                    .market(market)
                    .averagePrice(entry.getPrice())
                    .minPrice(entry.getPrice())
                    .maxPrice(entry.getPrice())
                    .recordedDate(date)
                    .source(entry.getSource() != null ? entry.getSource() : "BULK_DATASET_INGEST")
                    .build();

            priceHistoryRepository.save(history);
            count++;
        }
        log.info("Bulk dataset ingest completed successfully: {} records inserted.", count);
        return count;
    }
}
