package com.smartretail.microservices.aigovernance.controller;

import com.smartretail.pricemonitor.constants.ComplaintStatus;
import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.ComplaintRequest;
import com.smartretail.pricemonitor.dto.ComplaintResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Consumer complaint submission and authority investigation management")
@SecurityRequirement(name = "bearerAuth")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @Operation(summary = "Submit a new consumer complaint (price manipulation, fraud, etc.)")
    public ResponseEntity<ApiResponse<ComplaintResponse>> submitComplaint(
            Authentication authentication,
            @Valid @RequestBody ComplaintRequest request) {
        ComplaintResponse complaint = complaintService.submitComplaint(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully", complaint));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Get all customer complaints for Authority review")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> getAllComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(ApiResponse.success("All complaints retrieved",
                complaintService.getAllComplaints(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint by ID")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Complaint retrieved", complaintService.getComplaintById(id)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's complaints")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> getMyComplaints(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Your complaints retrieved",
                complaintService.getUserComplaints(authentication.getName(), page, size)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Get complaints by status (Authority / Admin)")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintResponse>>> getComplaintsByStatus(
            @PathVariable ComplaintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Complaints by status",
                complaintService.getComplaintsByStatus(status, page, size)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Update complaint investigation status (Authority / Admin)")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam ComplaintStatus status,
            @RequestParam(required = false) String resolutionNotes) {
        ComplaintResponse complaint = complaintService.updateComplaintStatus(
                id, status, resolutionNotes, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated to " + status, complaint));
    }
}
