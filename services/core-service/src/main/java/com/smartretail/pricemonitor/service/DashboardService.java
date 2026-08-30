package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.AdminDashboardResponse;
import com.smartretail.pricemonitor.dto.AuthorityDashboardResponse;
import com.smartretail.pricemonitor.dto.DashboardStatsResponse;

public interface DashboardService {
    DashboardStatsResponse getUserDashboard(String username);
    AuthorityDashboardResponse getAuthorityDashboard();
    AdminDashboardResponse getAdminDashboard();
}
