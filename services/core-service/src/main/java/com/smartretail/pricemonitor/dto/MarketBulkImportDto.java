package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketBulkImportDto {

    private String stateName;
    private String districtName;
    private String marketName;
    private String marketCode;
    private String city;
    private Double latitude;
    private Double longitude;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkImportResponse {
        private int totalProcessed;
        private int createdCount;
        private int skippedCount;
        private List<String> errorMessages;
    }
}
