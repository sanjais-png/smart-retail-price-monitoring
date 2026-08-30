package com.smartretail.pricemonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Regional Commodity Price Benchmark Dataset")
public class RegionalBenchmarkDto {

    @Schema(description = "Commodity ID", example = "1")
    private Long commodityId;

    @Schema(description = "Commodity Name", example = "Tomato")
    private String commodityName;

    @Schema(description = "State Name", example = "Tamil Nadu")
    private String stateName;

    @Schema(description = "District Name", example = "Chennai")
    private String districtName;

    @Schema(description = "Total Markets Monitored", example = "12")
    private Integer activeMarketsCount;

    @Schema(description = "Minimum Recorded Price in region", example = "35.00")
    private BigDecimal minPrice;

    @Schema(description = "Average Recorded Price in region", example = "42.50")
    private BigDecimal avgPrice;

    @Schema(description = "Maximum Recorded Price in region", example = "50.00")
    private BigDecimal maxPrice;

    @Schema(description = "Government MSP / Baseline Mandi Benchmark Price", example = "40.00")
    private BigDecimal governmentMspBenchmark;
}
