package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    private long totalPriceChecks;
    private long totalReportsSubmitted;
    private long activeAlertsCount;
    private long unreadNotificationsCount;
    private List<PriceReportResponse> recentReports;
    private List<AlertResponse> recentAlerts;
}
