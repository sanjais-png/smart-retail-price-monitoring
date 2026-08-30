package com.smartretail.pricemonitor.dto;

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
@Schema(description = "Exportable Dataset Record")
public class DatasetExportRecord {

    private Long reportId;
    private Long commodityId;
    private String commodityName;
    private String categoryName;
    private Long marketId;
    private String marketName;
    private String city;
    private String state;
    private BigDecimal reportedPrice;
    private String status;
    private LocalDate reportedDate;
}
