package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.PredictionTimeframe;
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
@Schema(description = "AI Price Prediction result")
public class PredictionResponse {

    @Schema(description = "Prediction ID", example = "101")
    private Long id;

    @Schema(description = "Commodity ID", example = "1")
    private Long commodityId;

    @Schema(description = "Commodity Name", example = "Rice - Sona Masoori")
    private String commodityName;

    @Schema(description = "Market ID", example = "1")
    private Long marketId;

    @Schema(description = "Market Name", example = "Koyambedu Wholesale Market")
    private String marketName;

    @Schema(description = "Target forecast date in ISO-8601 format (YYYY-MM-DD)", example = "2026-08-06")
    private LocalDate targetDate;

    @Schema(description = "Forecast timeframe window", example = "TOMORROW")
    private PredictionTimeframe timeframe;

    @Schema(description = "Predicted baseline price in INR", example = "58.75")
    private BigDecimal predictedPrice;

    @Schema(description = "Model confidence score (0.0 to 1.0)", example = "0.92")
    private Double confidenceScore;

    @Schema(description = "AI Model version used for forecasting", example = "HYBRID_V1_EXPONENTIAL_SMOOTHING")
    private String modelVersion;
}
