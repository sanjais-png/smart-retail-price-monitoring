package com.smartretail.microservices.fairness.controller;

import com.smartretail.pricemonitor.constants.ReportStatus;
import com.smartretail.pricemonitor.dto.*;
import com.smartretail.pricemonitor.service.PriceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Price Reports", description = "Community price report submission and authority verification workflow")
@SecurityRequirement(name = "bearerAuth")
public class PriceReportController {

    private final PriceReportService priceReportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit a community price report with optional image proof")
    public ResponseEntity<ApiResponse<PriceReportResponse>> submitReport(
            Authentication authentication,
            @RequestPart("data") @Valid PriceReportRequest request,
            @RequestPart(value = "imageProof", required = false) MultipartFile imageProof) {
        PriceReportResponse report = priceReportService.submitReport(authentication.getName(), request, imageProof);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Price report submitted successfully", report));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get price report by ID")
    public ResponseEntity<ApiResponse<PriceReportResponse>> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Report retrieved", priceReportService.getReportById(id)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's submitted price reports")
    public ResponseEntity<ApiResponse<PagedResponse<PriceReportResponse>>> getMyReports(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Your reports retrieved",
                priceReportService.getUserReports(authentication.getName(), page, size)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Get all pending reports (Authority / Admin)")
    public ResponseEntity<ApiResponse<PagedResponse<PriceReportResponse>>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Pending reports retrieved",
                priceReportService.getReportsByStatus(ReportStatus.PENDING, page, size)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY', 'ANALYST')")
    @Operation(summary = "Get reports filtered by status (PENDING, VERIFIED, REJECTED)")
    public ResponseEntity<ApiResponse<PagedResponse<PriceReportResponse>>> getReportsByStatus(
            @PathVariable ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Reports by status retrieved",
                priceReportService.getReportsByStatus(status, page, size)));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Verify or reject a price report (Authority / Admin)")
    public ResponseEntity<ApiResponse<PriceReportResponse>> verifyReport(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody VerifyReportRequest request) {
        PriceReportResponse report = priceReportService.verifyReport(authentication.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Report " + request.getStatus().name().toLowerCase() + " successfully", report));
    }
}
