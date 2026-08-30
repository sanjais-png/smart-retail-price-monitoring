package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.*;
import com.smartretail.pricemonitor.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "State, District, and Market hierarchy management")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/states")
    @Operation(summary = "Get all states")
    public ResponseEntity<ApiResponse<List<StateResponse>>> getAllStates() {
        return ResponseEntity.ok(ApiResponse.success("States retrieved", locationService.getAllStates()));
    }

    @GetMapping("/states/{stateId}/districts")
    @Operation(summary = "Get all districts in a state")
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getDistrictsByState(@PathVariable Long stateId) {
        return ResponseEntity.ok(ApiResponse.success("Districts retrieved",
                locationService.getDistrictsByState(stateId)));
    }

    @GetMapping("/districts/{districtId}/markets")
    @Operation(summary = "Get all markets in a district")
    public ResponseEntity<ApiResponse<List<MarketResponse>>> getMarketsByDistrict(@PathVariable Long districtId) {
        return ResponseEntity.ok(ApiResponse.success("Markets retrieved",
                locationService.getMarketsByDistrict(districtId)));
    }

    @GetMapping("/markets/city")
    @Operation(summary = "Get markets by city name")
    public ResponseEntity<ApiResponse<List<MarketResponse>>> getMarketsByCity(@RequestParam String city) {
        return ResponseEntity.ok(ApiResponse.success("Markets retrieved",
                locationService.getMarketsByCity(city)));
    }

    @GetMapping("/markets/search")
    @Operation(summary = "Search markets by name or city")
    public ResponseEntity<ApiResponse<PagedResponse<MarketResponse>>> searchMarkets(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Market search results",
                locationService.searchMarkets(query, page, size)));
    }

    @GetMapping("/markets/{id}")
    @Operation(summary = "Get market by ID")
    public ResponseEntity<ApiResponse<MarketResponse>> getMarketById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Market retrieved", locationService.getMarketById(id)));
    }

    @PostMapping("/markets")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Create a new market (Admin or Authority)")
    public ResponseEntity<ApiResponse<MarketResponse>> createMarket(@Valid @RequestBody MarketRequest request) {
        MarketResponse market = locationService.createMarket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Market created successfully", market));
    }

    @PostMapping("/markets/bulk-upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Nationwide Bulk Market Import", description = "Import thousands of official government/APMC markets in a single batch request")
    public ResponseEntity<ApiResponse<MarketBulkImportDto.BulkImportResponse>> bulkImportMarkets(
            @RequestBody List<MarketBulkImportDto> requests) {
        MarketBulkImportDto.BulkImportResponse response = locationService.bulkImportMarkets(requests);
        return ResponseEntity.ok(ApiResponse.success("Nationwide bulk market import completed", response));
    }
}
