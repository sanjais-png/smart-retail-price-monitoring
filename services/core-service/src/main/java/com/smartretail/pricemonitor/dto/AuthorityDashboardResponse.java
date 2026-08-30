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
public class AuthorityDashboardResponse {
    private long pendingReportsCount;
    private long verifiedReportsCount;
    private long rejectedReportsCount;
    private long pendingComplaintsCount;
    private List<PriceReportResponse> recentPendingReports;
    private List<ComplaintResponse> recentComplaints;
}
