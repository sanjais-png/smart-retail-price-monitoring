package com.smartretail.pricemonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request body for checking price fairness. Provide either marketId OR purchase location details (marketName + city OR GPS coordinates).")
public class FairnessCheckRequest {

    @NotNull(message = "Commodity ID is required")
    @Schema(description = "ID of the commodity being checked", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @Schema(
        description = "(OPTIONAL) Direct Market ID if selected from market list. Leave null/empty if specifying purchase shop location by name/city.",
        example = "1",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long marketId;

    @Schema(
        description = "(OPTIONAL) Purchase Shop or Market Name (e.g. Koyambedu Wholesale Market, Chennai Supermarket). Used when marketId is omitted.",
        example = "Koyambedu Wholesale Market",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String marketName;

    @Schema(
        description = "(OPTIONAL) Purchase City (e.g. Chennai, Coimbatore, Madurai). Used when marketId is omitted.",
        example = "Chennai",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String city;

    @Schema(
        description = "(OPTIONAL) Purchase State (e.g. Tamil Nadu, Kerala). Used when marketId is omitted.",
        example = "Tamil Nadu",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String state;

    @Schema(
        description = "(OPTIONAL) GPS Latitude of purchase shop location.",
        example = "13.0732",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Double latitude;

    @Schema(
        description = "(OPTIONAL) GPS Longitude of purchase shop location.",
        example = "80.1912",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Double longitude;

    @NotNull(message = "Purchase price is required")
    @Positive(message = "Purchase price must be positive")
    @Schema(description = "Price paid for the commodity", example = "42.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal purchasePrice;
}
