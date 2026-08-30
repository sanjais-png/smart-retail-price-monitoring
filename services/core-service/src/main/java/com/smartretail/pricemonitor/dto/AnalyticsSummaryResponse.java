package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummaryResponse {
    private String commodityName;
    private BigDecimal averagePrice;
    private BigDecimal medianPrice;
    private BigDecimal highestPrice;
    private BigDecimal lowestPrice;
    private List<RegionComparisonDto> regionalPrices;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegionComparisonDto {
        private String regionName;
        private BigDecimal averagePrice;
    }
}
