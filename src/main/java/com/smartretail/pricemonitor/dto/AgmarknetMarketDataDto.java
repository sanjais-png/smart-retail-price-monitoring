package com.smartretail.pricemonitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Official Government Agmarknet Market Record DTO")
public class AgmarknetMarketDataDto {

    @JsonProperty("state")
    @Schema(description = "State Name", example = "Tamil Nadu")
    private String state;

    @JsonProperty("district")
    @Schema(description = "District Name", example = "Coimbatore")
    private String district;

    @JsonProperty("market")
    @Schema(description = "Market / Mandi Name", example = "Coimbatore Uzhavar Sandhai")
    private String market;

    @JsonProperty("commodity")
    @Schema(description = "Commodity Name", example = "Onion")
    private String commodity;

    @JsonProperty("variety")
    @Schema(description = "Variety", example = "Red")
    private String variety;

    @JsonProperty("arrival_date")
    @Schema(description = "Arrival Date (DD/MM/YYYY)", example = "06/08/2026")
    private String arrivalDate;

    @JsonProperty("min_price")
    @Schema(description = "Min Wholesale Price in Rs/Quintal", example = "3200")
    private BigDecimal minPrice;

    @JsonProperty("max_price")
    @Schema(description = "Max Wholesale Price in Rs/Quintal", example = "4200")
    private BigDecimal maxPrice;

    @JsonProperty("modal_price")
    @Schema(description = "Modal prevailing Price in Rs/Quintal", example = "3700")
    private BigDecimal modalPrice;
}
