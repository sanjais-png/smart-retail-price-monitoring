package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.TrendDirection;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Market Trend Dataset Record")
public class MarketTrendDto {

    @Schema(description = "Trend Record ID", example = "1")
    private Long id;

    @Schema(description = "Commodity ID", example = "1")
    private Long commodityId;

    @Schema(description = "Commodity Name", example = "Rice (Basmati)")
    private String commodityName;

    @Schema(description = "Market ID", example = "1")
    private Long marketId;

    @Schema(description = "Market Name", example = "Koyambedu Wholesale Market")
    private String marketName;

    @Schema(description = "City", example = "Chennai")
    private String city;

    @Schema(description = "Period Type (DAILY, WEEKLY, MONTHLY)", example = "WEEKLY")
    private String periodType;

    @Schema(description = "Period Start Date", example = "2026-08-01")
    private LocalDate startDate;

    @Schema(description = "Period End Date", example = "2026-08-07")
    private LocalDate endDate;

    @Schema(description = "Average Price in period", example = "54.50")
    private BigDecimal avgPrice;

    @Schema(description = "Price Change Percentage", example = "+3.2")
    private Double priceChangePercentage;

    @Schema(description = "Trend Direction (RISING, FALLING, STABLE)", example = "RISING")
    private TrendDirection trendDirection;
}
