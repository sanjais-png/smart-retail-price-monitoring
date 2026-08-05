package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.PredictionTimeframe;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for AI Price Prediction. No date input is needed — the system automatically calculates the target date based on the chosen timeframe.")
public class PredictionRequest {

    @NotNull(message = "Commodity ID is required")
    @Schema(description = "ID of the commodity to predict", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @NotNull(message = "Market ID is required")
    @Schema(description = "ID of the market location", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long marketId;

    @NotNull(message = "Prediction timeframe is required")
    @Schema(
        description = "Target forecast window: TOMORROW (+1 day), NEXT_WEEK (+7 days), or NEXT_MONTH (+30 days)",
        example = "TOMORROW",
        allowableValues = {"TOMORROW", "NEXT_WEEK", "NEXT_MONTH"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private PredictionTimeframe timeframe;
}
