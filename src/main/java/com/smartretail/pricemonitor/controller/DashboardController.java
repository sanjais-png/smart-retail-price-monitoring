package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.AdminDashboardResponse;
import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.AuthorityDashboardResponse;
import com.smartretail.pricemonitor.dto.DashboardStatsResponse;
import com.smartretail.pricemonitor.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Role-specific dashboard statistics and summaries")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get User Dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getUserDashboard(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("User dashboard retrieved",
                dashboardService.getUserDashboard(authentication.getName())));
    }

    @GetMapping("/authority")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @Operation(summary = "Get Authority Dashboard statistics")
    public ResponseEntity<ApiResponse<AuthorityDashboardResponse>> getAuthorityDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Authority dashboard retrieved",
                dashboardService.getAuthorityDashboard()));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Admin Dashboard statistics")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard retrieved",
                dashboardService.getAdminDashboard()));
    }
}
