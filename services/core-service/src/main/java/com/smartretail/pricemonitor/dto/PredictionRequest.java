package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.PredictionTimeframe;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for AI Price Prediction.")
public class PredictionRequest {

    @NotNull(message = "Commodity ID is required")
    private Long commodityId;

    @NotNull(message = "Market ID is required")
    private Long marketId;

    @NotNull(message = "Prediction timeframe is required")
    private PredictionTimeframe timeframe;
}
