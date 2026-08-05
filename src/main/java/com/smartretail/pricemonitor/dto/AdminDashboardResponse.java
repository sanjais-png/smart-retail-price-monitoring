package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalAuthorities;
    private long totalCommodities;
    private long totalMarkets;
    private long totalReports;
    private long totalComplaints;
}
