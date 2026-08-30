package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.PriceHistoryResponse;
import com.smartretail.pricemonitor.entity.PriceHistory;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.PriceHistoryRepository;
import com.smartretail.pricemonitor.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PriceHistoryResponse> getPriceHistory(Long commodityId, Long marketId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        return priceHistoryRepository.findByCommodityIdAndMarketIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                commodityId, marketId, startDate, endDate
        ).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PriceHistoryResponse getLatestPrice(Long commodityId, Long marketId) {
        PriceHistory history = priceHistoryRepository.findLatestPrice(commodityId, marketId)
                .orElseThrow(() -> new ResourceNotFoundException("Price history not found for commodityId: " + commodityId + ", marketId: " + marketId));
        return mapToResponse(history);
    }

    private PriceHistoryResponse mapToResponse(PriceHistory ph) {
        return PriceHistoryResponse.builder()
                .id(ph.getId())
                .commodityId(ph.getCommodity().getId())
                .commodityName(ph.getCommodity().getName())
                .marketId(ph.getMarket().getId())
                .marketName(ph.getMarket().getName())
                .averagePrice(ph.getAveragePrice())
                .minPrice(ph.getMinPrice())
                .maxPrice(ph.getMaxPrice())
                .recordedDate(ph.getRecordedDate())
                .source(ph.getSource())
                .build();
    }
}
