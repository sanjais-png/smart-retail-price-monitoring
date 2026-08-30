package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.FairnessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FairnessCheckResponse {
    private Long id;
    private Long commodityId;
    private String commodityName;
    private Long marketId;
    private String marketName;
    private BigDecimal purchasePrice;
    private BigDecimal marketPrice;
    private BigDecimal priceDifference;
    private Double percentageDifference;
    private Integer fairnessScore; // 0 - 100
    private FairnessStatus status;
    private String recommendation;
    private Double confidenceScore;
    private Boolean isColdStart;
    private String baselineSource;
    private LocalDateTime calculatedAt;
}
