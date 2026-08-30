package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.ReceiptAnalysisResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptOcrService {
    ReceiptAnalysisResponse parseReceiptImage(MultipartFile file);
}
