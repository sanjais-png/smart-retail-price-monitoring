package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptAnalysisResponse {

    private String storeName;
    private LocalDate purchaseDate;
    private String detectedCommodity;
    private BigDecimal extractedPrice;
    private String unit;
    private double confidenceScore;
    private List<ExtractedItem> extractedItems;
    private String rawOcrText;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedItem {
        private String itemName;
        private BigDecimal price;
        private String quantity;
    }
}
