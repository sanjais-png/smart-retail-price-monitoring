package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.TrendDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceTrendResponse {
    private Long id;
    private Long commodityId;
    private String commodityName;
    private Long marketId;
    private String marketName;
    private String periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal avgPrice;
    private Double priceChangePercentage;
    private TrendDirection trendDirection;
}
