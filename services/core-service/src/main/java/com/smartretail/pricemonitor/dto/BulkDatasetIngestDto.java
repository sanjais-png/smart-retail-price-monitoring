package com.smartretail.pricemonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Dataset entry for bulk ingestion")
public class BulkDatasetIngestDto {

    @NotNull(message = "Commodity ID is required")
    @Schema(description = "Commodity ID", example = "1")
    private Long commodityId;

    @Schema(description = "Commodity Name (Optional if ID provided)", example = "Onion")
    private String commodityName;

    @Schema(description = "Market Name", example = "Dadar APMC Market")
    private String marketName;

    @Schema(description = "City", example = "Mumbai")
    private String city;

    @Schema(description = "State", example = "Maharashtra")
    private String state;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(description = "Recorded Price", example = "38.50")
    private BigDecimal price;

    @Schema(description = "Record Date (Defaults to today if omitted)", example = "2026-08-05")
    private LocalDate recordedDate;

    @Schema(description = "Data Source tag", example = "GOVT_AGMARKNET")
    private String source;
}
