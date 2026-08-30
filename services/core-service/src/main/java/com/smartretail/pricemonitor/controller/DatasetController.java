package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.*;
import com.smartretail.pricemonitor.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/datasets")
@RequiredArgsConstructor
@Tag(name = "Dataset APIs & Analytics Data Export", description = "Public & Analyst APIs for Market Trends Datasets, Regional Benchmarks, Bulk CSV/JSON Exports, and Dataset Ingestion")
public class DatasetController {

    private final DatasetService datasetService;

    @GetMapping("/market-trends")
    @Operation(
        summary = "Get Market Trends Dataset",
        description = "Retrieve historical time-series market trend datasets filtered by commodity, market, and period type (DAILY, WEEKLY, MONTHLY)."
    )
    public ResponseEntity<ApiResponse<List<MarketTrendDto>>> getMarketTrends(
            @RequestParam Long commodityId,
            @RequestParam Long marketId,
            @RequestParam(required = false, defaultValue = "WEEKLY") String periodType) {

        List<MarketTrendDto> trends = datasetService.getMarketTrendsDataset(commodityId, marketId, periodType);
        return ResponseEntity.ok(ApiResponse.success("Market trends dataset retrieved successfully", trends));
    }

    @GetMapping("/regional-benchmarks")
    @Operation(
        summary = "Get Regional Commodity Price Benchmarks Dataset",
        description = "Returns min, max, average retail price benchmarks and government MSP comparison for commodities across states and districts."
    )
    public ResponseEntity<ApiResponse<List<RegionalBenchmarkDto>>> getRegionalBenchmarks(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city) {

        List<RegionalBenchmarkDto> benchmarks = datasetService.getRegionalPriceBenchmarks(state, city);
        return ResponseEntity.ok(ApiResponse.success("Regional price benchmark dataset retrieved successfully", benchmarks));
    }

    @GetMapping("/export/json")
    @Operation(
        summary = "Export Bulk Price Dataset (JSON)",
        description = "Export complete price monitoring dataset in JSON format for data science, machine learning models, and research."
    )
    public ResponseEntity<ApiResponse<List<DatasetExportRecord>>> exportDatasetJson(
            @RequestParam(required = false) Long commodityId,
            @RequestParam(required = false) String state) {

        List<DatasetExportRecord> dataset = datasetService.getCompleteExportDataset(commodityId, state);
        return ResponseEntity.ok(ApiResponse.success("Price dataset exported successfully", dataset));
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    @Operation(
        summary = "Export Bulk Price Dataset (CSV Download)",
        description = "Download full price monitoring dataset as a CSV file for Excel, Pandas, or analytical tools."
    )
    public ResponseEntity<String> exportDatasetCsv(
            @RequestParam(required = false) Long commodityId,
            @RequestParam(required = false) String state) {

        String csvData = datasetService.exportDatasetAsCsv(commodityId, state);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"smart_retail_price_dataset.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @PostMapping("/bulk-ingest")
    @Operation(
        summary = "Bulk Dataset Ingestion API",
        description = "Mass ingest price dataset records from external government databases (Agmarknet), NGO feeds, or CSV files."
    )
    public ResponseEntity<ApiResponse<String>> bulkIngestDataset(
            @Valid @RequestBody List<BulkDatasetIngestDto> datasetEntries) {

        int insertedCount = datasetService.bulkIngestDataset(datasetEntries);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Bulk dataset ingestion completed! Successfully ingested %d records.", insertedCount),
                String.format("%d records processed", insertedCount)
        ));
    }
}
