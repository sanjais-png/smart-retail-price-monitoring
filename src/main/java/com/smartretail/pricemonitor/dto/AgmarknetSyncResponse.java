package com.smartretail.pricemonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Agmarknet Dataset Synchronization Status & Summary")
public class AgmarknetSyncResponse {

    private boolean success;
    private String statusMessage;
    private int totalStatesCovered;
    private int totalDistrictsCovered;
    private int totalMarketsSynced;
    private int totalCommoditiesUpdated;
    private int totalPriceRecordsIngested;
    private String dataSource;
    private LocalDateTime syncedAt;
    private List<String> coveredStatesList;
}
