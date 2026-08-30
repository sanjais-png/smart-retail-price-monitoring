package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.analytics.AnalyticsService;
import com.smartretail.pricemonitor.dto.AnalyticsSummaryResponse;
import com.smartretail.pricemonitor.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Price analytics and statistical comparisons")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/commodity")
    @Operation(summary = "Get detailed analytics for a commodity across markets")
    public ResponseEntity<ApiResponse<AnalyticsSummaryResponse>> getCommodityAnalytics(
            @RequestParam Long commodityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved",
                analyticsService.getCommodityAnalytics(commodityId, startDate, endDate)));
    }
}
