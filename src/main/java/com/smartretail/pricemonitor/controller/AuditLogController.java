package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.AuditLogResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "System audit trail for compliance (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all system audit logs")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved",
                auditLogService.getAllLogs(page, size)));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs for a specific user")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getLogsByUser(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Audit logs for user retrieved",
                auditLogService.getLogsByUser(userId, page, size)));
    }
}
