package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.AlertType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRequest {

    @NotNull(message = "Commodity ID is required")
    private Long commodityId;

    @NotNull(message = "Market ID is required")
    private Long marketId;

    @NotNull(message = "Alert type is required")
    private AlertType alertType;

    @NotNull(message = "Target price threshold is required")
    @Positive
    private BigDecimal targetPrice;
}
