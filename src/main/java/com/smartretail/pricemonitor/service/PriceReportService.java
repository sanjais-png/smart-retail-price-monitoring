package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.constants.ReportStatus;
import com.smartretail.pricemonitor.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface PriceReportService {
    PriceReportResponse submitReport(String username, PriceReportRequest request, MultipartFile imageProof);
    PriceReportResponse verifyReport(String authorityUsername, Long reportId, VerifyReportRequest request);
    PriceReportResponse getReportById(Long id);
    PagedResponse<PriceReportResponse> getUserReports(String username, int page, int size);
    PagedResponse<PriceReportResponse> getReportsByStatus(ReportStatus status, int page, int size);
}
