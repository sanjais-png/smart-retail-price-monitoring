package com.smartretail.pricemonitor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PriceReportRequest {

    @NotNull(message = "Commodity ID is required")
    private Long commodityId;

    private Long marketId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private String vendorName;

    private Double latitude;
    private Double longitude;
    private String marketName;
    private String city;
    private String state;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    private String description;
}
