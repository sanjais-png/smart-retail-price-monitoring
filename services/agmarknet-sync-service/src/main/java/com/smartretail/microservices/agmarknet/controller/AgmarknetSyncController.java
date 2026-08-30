package com.smartretail.microservices.agmarknet.controller;

import com.smartretail.pricemonitor.dto.AgmarknetMarketDataDto;
import com.smartretail.pricemonitor.dto.AgmarknetSyncResponse;
import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.service.AgmarknetIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/datasets/agmarknet")
@RequiredArgsConstructor
@Tag(name = "Agmarknet Live Government Data & Master Market Sync", description = "Official Government of India Agmarknet Dataset Integration (data.gov.in) covering 3,000+ Mandis across all Indian States")
public class AgmarknetSyncController {

    private final AgmarknetIntegrationService agmarknetIntegrationService;

    @PostMapping("/seed-master")
    @Operation(
        summary = "Seed Nationwide Indian Markets Dataset",
        description = "Populates master Indian Mandis & Retail Markets across Tamil Nadu, Maharashtra, Delhi NCR, Karnataka, Kerala, AP, UP, West Bengal, and pre-seeds baseline prices."
    )
    public ResponseEntity<ApiResponse<AgmarknetSyncResponse>> seedNationwideMasterDataset() {
        AgmarknetSyncResponse response = agmarknetIntegrationService.seedNationwideMasterDataset();
        return ResponseEntity.ok(ApiResponse.success("Nationwide Indian Markets & Agmarknet Master Dataset seeded successfully", response));
    }

    @PostMapping("/sync")
    @Operation(
        summary = "Trigger Live Sync from Government Agmarknet API",
        description = "Fetch live daily price data directly from data.gov.in Agmarknet API endpoint."
    )
    public ResponseEntity<ApiResponse<AgmarknetSyncResponse>> syncLiveAgmarknetData(
            @RequestParam(required = false) String apiKey) {

        AgmarknetSyncResponse response = agmarknetIntegrationService.syncFromLiveGovernmentApi(apiKey);
        return ResponseEntity.ok(ApiResponse.success("Live Agmarknet government data sync completed", response));
    }

    @PostMapping("/ingest-batch")
    @Operation(
        summary = "Ingest Custom Batch of Agmarknet Market Records",
        description = "Allows batch ingestion of official Agmarknet JSON records from Govt data portals."
    )
    public ResponseEntity<ApiResponse<AgmarknetSyncResponse>> ingestBatch(
            @RequestBody List<AgmarknetMarketDataDto> records) {

        AgmarknetSyncResponse response = agmarknetIntegrationService.ingestAgmarknetData(records);
        return ResponseEntity.ok(ApiResponse.success("Agmarknet batch ingestion completed", response));
    }

    @GetMapping("/status")
    @Operation(
        summary = "Get Agmarknet Dataset Coverage & Status",
        description = "View total states, districts, markets, and price reports synced in MySQL database."
    )
    public ResponseEntity<ApiResponse<AgmarknetSyncResponse>> getSyncStatus() {
        AgmarknetSyncResponse status = agmarknetIntegrationService.getSyncStatus();
        return ResponseEntity.ok(ApiResponse.success("Agmarknet dataset sync status retrieved", status));
    }
}
