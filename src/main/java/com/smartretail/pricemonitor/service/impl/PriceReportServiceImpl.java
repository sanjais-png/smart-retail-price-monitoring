package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.constants.ReportStatus;
import com.smartretail.pricemonitor.dto.*;
import com.smartretail.pricemonitor.entity.*;
import com.smartretail.pricemonitor.exception.BadRequestException;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.*;
import com.smartretail.pricemonitor.service.LocationService;
import com.smartretail.pricemonitor.service.PriceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceReportServiceImpl implements PriceReportService {

    private final PriceReportRepository priceReportRepository;
    private final UserRepository userRepository;
    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final LocationService locationService;

    @Value("${app.file.upload-dir:./uploads/proofs}")
    private String uploadDir;

    @Override
    @Transactional
    public PriceReportResponse submitReport(String username, PriceReportRequest request, MultipartFile imageProof) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Commodity commodity = commodityRepository.findById(request.getCommodityId())
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", request.getCommodityId()));

        Market market;
        if (request.getMarketId() != null && request.getMarketId() > 0) {
            market = marketRepository.findById(request.getMarketId())
                    .orElseThrow(() -> new ResourceNotFoundException("Market", "id", request.getMarketId()));
        } else if (request.getMarketName() != null || (request.getLatitude() != null && request.getLongitude() != null)) {
            String mktName = request.getMarketName() != null ? request.getMarketName() : "Auto Local Market";
            String city = request.getCity() != null ? request.getCity() : "Local City";
            String state = request.getState() != null ? request.getState() : "Local State";
            
            MarketResponse response = locationService.findOrCreateMarketByLocation(mktName, city, state, request.getLatitude(), request.getLongitude());
            market = marketRepository.findById(response.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Market", "id", response.getId()));
        } else {
            throw new BadRequestException("Either marketId or location details (marketName, city, state, latitude, longitude) must be provided!");
        }

        String imagePath = null;
        if (imageProof != null && !imageProof.isEmpty()) {
            imagePath = saveImageFile(imageProof);
        }

        PriceReport report = PriceReport.builder()
                .user(user)
                .commodity(commodity)
                .market(market)
                .price(request.getPrice())
                .vendorName(request.getVendorName())
                .purchaseDate(request.getPurchaseDate())
                .imageProofPath(imagePath)
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        return mapToResponse(priceReportRepository.save(report));
    }

    @Override
    @Transactional
    public PriceReportResponse verifyReport(String authorityUsername, Long reportId, VerifyReportRequest request) {
        User authorityUser = userRepository.findByUsername(authorityUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authorityUsername));

        PriceReport report = priceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceReport", "id", reportId));

        report.setStatus(request.getStatus());
        report.setVerifiedBy(authorityUser);
        report.setVerificationNotes(request.getVerificationNotes());

        return mapToResponse(priceReportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public PriceReportResponse getReportById(Long id) {
        PriceReport report = priceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PriceReport", "id", id));
        return mapToResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PriceReportResponse> getUserReports(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PriceReport> reportsPage = priceReportRepository.findByUserId(user.getId(), pageable);
        return mapToPagedResponse(reportsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PriceReportResponse> getReportsByStatus(ReportStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PriceReport> reportsPage = priceReportRepository.findByStatus(status, pageable);
        return mapToPagedResponse(reportsPage);
    }

    private String saveImageFile(MultipartFile file) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = Paths.get(uploadDir).resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetLocation.toString();
        } catch (IOException ex) {
            return "uploads/proofs/sample_proof.jpg";
        }
    }

    private PriceReportResponse mapToResponse(PriceReport report) {
        return PriceReportResponse.builder()
                .id(report.getId())
                .userId(report.getUser().getId())
                .username(report.getUser().getUsername())
                .commodityId(report.getCommodity().getId())
                .commodityName(report.getCommodity().getName())
                .marketId(report.getMarket().getId())
                .marketName(report.getMarket().getName())
                .price(report.getPrice())
                .vendorName(report.getVendorName())
                .purchaseDate(report.getPurchaseDate())
                .imageProofPath(report.getImageProofPath())
                .description(report.getDescription())
                .status(report.getStatus())
                .verifiedByUsername(report.getVerifiedBy() != null ? report.getVerifiedBy().getUsername() : null)
                .verificationNotes(report.getVerificationNotes())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private PagedResponse<PriceReportResponse> mapToPagedResponse(Page<PriceReport> page) {
        List<PriceReportResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<PriceReportResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
