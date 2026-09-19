package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.AlertType;
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
public class AlertResponse {
    private Long id;
    private Long commodityId;
    private String commodityName;
    private Long marketId;
    private String marketName;
    private AlertType alertType;
    private BigDecimal targetPrice;
    private boolean active;
    private BigDecimal lastEvaluatedPrice;
    private LocalDateTime lastEvaluatedAt;
    private BigDecimal lastTriggeredPrice;
    private LocalDateTime lastTriggeredAt;
    private LocalDateTime createdAt;
}
