package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.PriceHistoryResponse;
import com.smartretail.pricemonitor.service.PriceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@Tag(name = "Price History", description = "Historical price data retrieval with date range filters")
@SecurityRequirement(name = "bearerAuth")
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    @GetMapping
    @Operation(summary = "Get price history for a commodity and market with optional date range")
    public ResponseEntity<ApiResponse<List<PriceHistoryResponse>>> getPriceHistory(
            @RequestParam Long commodityId,
            @RequestParam Long marketId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success("Price history retrieved",
                priceHistoryService.getPriceHistory(commodityId, marketId, startDate, endDate)));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get the latest recorded price for a commodity in a market")
    public ResponseEntity<ApiResponse<PriceHistoryResponse>> getLatestPrice(
            @RequestParam Long commodityId,
            @RequestParam Long marketId) {
        return ResponseEntity.ok(ApiResponse.success("Latest price retrieved",
                priceHistoryService.getLatestPrice(commodityId, marketId)));
    }
}
