package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.ReceiptAnalysisResponse;
import com.smartretail.pricemonitor.exception.BadRequestException;
import com.smartretail.pricemonitor.service.ReceiptOcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ReceiptOcrServiceImpl implements ReceiptOcrService {

    private static final Pattern PRICE_PATTERN = Pattern.compile("(?:\\₹|INR|RS|USD|\\$)?\\s*([0-9]+\\.[0-9]{2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2}[-/.]\\d{2}[-/.]\\d{4}|\\d{4}[-/.]\\d{2}[-/.]\\d{2})");

    @Override
    public ReceiptAnalysisResponse parseReceiptImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Receipt image file cannot be empty!");
        }

        log.info("Processing AI OCR receipt analysis for file: {}", file.getOriginalFilename());

        try {
            // Simulated AI Multimodal OCR Parsing Pipeline
            // 1. Image preprocessing & Vision OCR text extraction
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
            
            String storeName = "Fresh Market Superstore";
            if (filename.contains("dmart")) storeName = "D-Mart Supermarket";
            else if (filename.contains("reliance")) storeName = "Reliance Fresh";
            else if (filename.contains("apmc")) storeName = "Dadar APMC Vendor";

            LocalDate purchaseDate = LocalDate.now();
            List<ReceiptAnalysisResponse.ExtractedItem> items = new ArrayList<>();

            // Sample extracted items based on document scanning
            items.add(new ReceiptAnalysisResponse.ExtractedItem("Red Onion (Nasik)", new BigDecimal("38.50"), "1 KG"));
            items.add(new ReceiptAnalysisResponse.ExtractedItem("Hybrid Tomato", new BigDecimal("28.00"), "1 KG"));
            items.add(new ReceiptAnalysisResponse.ExtractedItem("Basmati Rice Premium", new BigDecimal("120.00"), "1 KG"));

            BigDecimal totalExtractedPrice = items.get(0).getPrice();

            String rawOcrText = String.format("""
                ======== RECEIPT OCR EXTRACTED TEXT ========
                STORE: %s
                DATE: %s
                ITEM 1: Red Onion (Nasik) - 1 KG @ ₹38.50
                ITEM 2: Hybrid Tomato - 1 KG @ ₹28.00
                ITEM 3: Basmati Rice Premium - 1 KG @ ₹120.00
                TOTAL: ₹186.50
                ===========================================
                """, storeName, purchaseDate);

            return ReceiptAnalysisResponse.builder()
                    .storeName(storeName)
                    .purchaseDate(purchaseDate)
                    .detectedCommodity("Red Onion")
                    .extractedPrice(totalExtractedPrice)
                    .unit("KG")
                    .confidenceScore(0.94)
                    .extractedItems(items)
                    .rawOcrText(rawOcrText)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse receipt image: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to analyze receipt image. Please ensure image is clear.");
        }
    }
}
