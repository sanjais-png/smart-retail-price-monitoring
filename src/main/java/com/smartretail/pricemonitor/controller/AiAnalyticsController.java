package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.AnomalyAnalysisResponse;
import com.smartretail.pricemonitor.dto.ReceiptAnalysisResponse;
import com.smartretail.pricemonitor.service.PriceAnomalyDetectionService;
import com.smartretail.pricemonitor.service.ReceiptOcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI & ML Intelligence", description = "AI OCR Receipt Parsing & Statistical Price Gouging Anomaly Detection")
public class AiAnalyticsController {

    private final ReceiptOcrService receiptOcrService;
    private final PriceAnomalyDetectionService anomalyDetectionService;

    @PostMapping(value = "/parse-receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "AI OCR Receipt Parsing", description = "Upload a retail receipt or invoice image to automatically extract store name, purchase date, item names, and prices")
    public ResponseEntity<ApiResponse<ReceiptAnalysisResponse>> parseReceipt(@RequestParam("file") MultipartFile file) {
        ReceiptAnalysisResponse response = receiptOcrService.parseReceiptImage(file);
        return ResponseEntity.ok(ApiResponse.success("Receipt parsed successfully with AI OCR", response));
    }

    @GetMapping("/detect-anomaly")
    @Operation(summary = "Z-Score Price Anomaly & Gouging Detection", description = "Evaluates statistical Z-score dispersion to detect artificial price hikes and store collusion risks")
    public ResponseEntity<ApiResponse<AnomalyAnalysisResponse>> detectAnomaly(
            @RequestParam Long commodityId,
            @RequestParam Long marketId,
            @RequestParam BigDecimal targetPrice) {
        AnomalyAnalysisResponse response = anomalyDetectionService.detectAnomaly(commodityId, marketId, targetPrice);
        return ResponseEntity.ok(ApiResponse.success("Price anomaly analysis evaluated successfully", response));
    }
}
