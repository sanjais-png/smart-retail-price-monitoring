package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceReportResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long commodityId;
    private String commodityName;
    private Long marketId;
    private String marketName;
    private BigDecimal price;
    private String vendorName;
    private LocalDate purchaseDate;
    private String imageProofPath;
    private String description;
    private ReportStatus status;
    private String verifiedByUsername;
    private String verificationNotes;
    private LocalDateTime createdAt;
}
