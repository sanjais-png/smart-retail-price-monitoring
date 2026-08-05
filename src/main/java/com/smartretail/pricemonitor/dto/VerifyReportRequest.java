package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyReportRequest {
    @NotNull(message = "Report status (VERIFIED/REJECTED) is required")
    private ReportStatus status;

    private String verificationNotes;
}
