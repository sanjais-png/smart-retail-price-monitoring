package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.constants.ComplaintStatus;
import com.smartretail.pricemonitor.constants.ReportStatus;
import com.smartretail.pricemonitor.dto.*;
import com.smartretail.pricemonitor.entity.User;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.*;
import com.smartretail.pricemonitor.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final PriceReportRepository priceReportRepository;
    private final AlertRepository alertRepository;
    private final NotificationRepository notificationRepository;
    private final ComplaintRepository complaintRepository;
    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final AuthorityRepository authorityRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getUserDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        PageRequest recentPageRequest = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        List<PriceReportResponse> recentReports = priceReportRepository
                .findByUserId(user.getId(), recentPageRequest)
                .getContent().stream()
                .map(r -> PriceReportResponse.builder()
                        .id(r.getId())
                        .commodityName(r.getCommodity().getName())
                        .marketName(r.getMarket().getName())
                        .price(r.getPrice())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<AlertResponse> recentAlerts = alertRepository
                .findByUserId(user.getId(), recentPageRequest)
                .getContent().stream()
                .map(a -> AlertResponse.builder()
                        .id(a.getId())
                        .commodityName(a.getCommodity().getName())
                        .marketName(a.getMarket().getName())
                        .alertType(a.getAlertType())
                        .targetPrice(a.getTargetPrice())
                        .active(a.isActive())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        long unreadNotifications = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        long totalReports = priceReportRepository.findByUserId(user.getId(), PageRequest.of(0, 1)).getTotalElements();
        long activeAlerts = alertRepository.findByUserId(user.getId(), PageRequest.of(0, 1)).getTotalElements();

        return DashboardStatsResponse.builder()
                .totalPriceChecks(0L) // tracked via fairness result count per user
                .totalReportsSubmitted(totalReports)
                .activeAlertsCount(activeAlerts)
                .unreadNotificationsCount(unreadNotifications)
                .recentReports(recentReports)
                .recentAlerts(recentAlerts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorityDashboardResponse getAuthorityDashboard() {
        long pending = priceReportRepository.countByStatus(ReportStatus.PENDING);
        long verified = priceReportRepository.countByStatus(ReportStatus.VERIFIED);
        long rejected = priceReportRepository.countByStatus(ReportStatus.REJECTED);
        long pendingComplaints = complaintRepository.countByStatus(ComplaintStatus.PENDING);

        PageRequest recentPageRequest = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        List<PriceReportResponse> recentPendingReports = priceReportRepository
                .findByStatus(ReportStatus.PENDING, recentPageRequest)
                .getContent().stream()
                .map(r -> PriceReportResponse.builder()
                        .id(r.getId())
                        .username(r.getUser().getUsername())
                        .commodityName(r.getCommodity().getName())
                        .marketName(r.getMarket().getName())
                        .price(r.getPrice())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<ComplaintResponse> recentComplaints = complaintRepository
                .findByStatus(ComplaintStatus.PENDING, recentPageRequest)
                .getContent().stream()
                .map(c -> ComplaintResponse.builder()
                        .id(c.getId())
                        .username(c.getUser().getUsername())
                        .title(c.getTitle())
                        .marketName(c.getMarket().getName())
                        .status(c.getStatus())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return AuthorityDashboardResponse.builder()
                .pendingReportsCount(pending)
                .verifiedReportsCount(verified)
                .rejectedReportsCount(rejected)
                .pendingComplaintsCount(pendingComplaints)
                .recentPendingReports(recentPendingReports)
                .recentComplaints(recentComplaints)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        long totalUsers = userRepository.count();
        long totalAuthorities = authorityRepository.count();
        long totalCommodities = commodityRepository.count();
        long totalMarkets = marketRepository.count();
        long totalReports = priceReportRepository.count();
        long totalComplaints = complaintRepository.count();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalAuthorities(totalAuthorities)
                .totalCommodities(totalCommodities)
                .totalMarkets(totalMarkets)
                .totalReports(totalReports)
                .totalComplaints(totalComplaints)
                .build();
    }
}
