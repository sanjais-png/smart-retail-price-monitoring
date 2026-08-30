package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.AlertRequest;
import com.smartretail.pricemonitor.dto.AlertResponse;
import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Price Alerts", description = "Subscribe to price alerts for commodities. Get notified on price increases, decreases, or threshold crossings.")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    @Operation(summary = "Create a new price alert subscription")
    public ResponseEntity<ApiResponse<AlertResponse>> createAlert(
            Authentication authentication,
            @Valid @RequestBody AlertRequest request) {
        AlertResponse alert = alertService.createAlert(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Price alert created successfully", alert));
    }

    @GetMapping
    @Operation(summary = "Get all price alerts for current user")
    public ResponseEntity<ApiResponse<PagedResponse<AlertResponse>>> getMyAlerts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved",
                alertService.getUserAlerts(authentication.getName(), page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get price alert by ID")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Alert retrieved", alertService.getAlertById(id)));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle price alert active/inactive")
    public ResponseEntity<ApiResponse<AlertResponse>> toggleAlert(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam boolean active) {
        AlertResponse alert = alertService.toggleAlert(authentication.getName(), id, active);
        return ResponseEntity.ok(ApiResponse.success("Alert " + (active ? "activated" : "deactivated"), alert));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a price alert")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(
            Authentication authentication,
            @PathVariable Long id) {
        alertService.deleteAlert(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Alert deleted successfully"));
    }
}
