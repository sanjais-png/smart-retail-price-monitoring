package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.CommodityRequest;
import com.smartretail.pricemonitor.dto.CommodityResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.service.CommodityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/commodities")
@RequiredArgsConstructor
@Tag(name = "Commodities", description = "Commodity search, browse, and management")
@SecurityRequirement(name = "bearerAuth")
public class CommodityController {

    private final CommodityService commodityService;

    @GetMapping
    @Operation(summary = "Get all commodities with pagination and sorting")
    public ResponseEntity<ApiResponse<PagedResponse<CommodityResponse>>> getAllCommodities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "300") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success("Commodities retrieved",
                commodityService.getAllCommodities(page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get commodity by ID")
    public ResponseEntity<ApiResponse<CommodityResponse>> getCommodityById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Commodity retrieved", commodityService.getCommodityById(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get commodity by code")
    public ResponseEntity<ApiResponse<CommodityResponse>> getCommodityByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("Commodity retrieved", commodityService.getCommodityByCode(code)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search commodities by name or code")
    public ResponseEntity<ApiResponse<PagedResponse<CommodityResponse>>> searchCommodities(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Search results",
                commodityService.searchCommodities(query, page, size)));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get commodities by category")
    public ResponseEntity<ApiResponse<PagedResponse<CommodityResponse>>> getCommoditiesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Commodities by category retrieved",
                commodityService.getCommoditiesByCategory(categoryId, page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new commodity (Admin only)")
    public ResponseEntity<ApiResponse<CommodityResponse>> createCommodity(@Valid @RequestBody CommodityRequest request) {
        CommodityResponse commodity = commodityService.createCommodity(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Commodity created successfully", commodity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a commodity (Admin only)")
    public ResponseEntity<ApiResponse<CommodityResponse>> updateCommodity(
            @PathVariable Long id,
            @Valid @RequestBody CommodityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Commodity updated", commodityService.updateCommodity(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate (soft-delete) a commodity (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCommodity(@PathVariable Long id) {
        commodityService.deleteCommodity(id);
        return ResponseEntity.ok(ApiResponse.success("Commodity deactivated successfully"));
    }
}
