package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyAnalysisResponse {

    private Long commodityId;
    private String commodityName;
    private Long marketId;
    private String marketName;
    private BigDecimal evaluatedPrice;
    private BigDecimal marketMeanPrice;
    private BigDecimal standardDeviation;
    private double zScore;
    private boolean isAnomalous;
    private boolean isCollusionRisk;
    private String riskLevel;
    private String explanation;
}
