package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.constants.ComplaintStatus;
import com.smartretail.pricemonitor.dto.ComplaintRequest;
import com.smartretail.pricemonitor.dto.ComplaintResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Complaint;
import com.smartretail.pricemonitor.entity.District;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.State;
import com.smartretail.pricemonitor.entity.User;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.*;
import com.smartretail.pricemonitor.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    private final CommodityRepository commodityRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;

    @Override
    @Transactional
    public ComplaintResponse submitComplaint(String username, ComplaintRequest request) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Market market = null;
        if (request.getMarketId() != null && request.getMarketId() > 0) {
            market = marketRepository.findById(request.getMarketId()).orElse(null);
        }
        if (market == null) {
            market = marketRepository.findAll().stream().findFirst().orElse(null);
        }
        if (market == null) {
            State defaultState = stateRepository.findAll().stream().findFirst()
                    .orElseGet(() -> stateRepository.save(State.builder().name("Tamil Nadu").code("TN").build()));
            District defaultDistrict = districtRepository.findAll().stream().findFirst()
                    .orElseGet(() -> districtRepository.save(District.builder().name("Coimbatore").state(defaultState).build()));
            market = marketRepository.save(Market.builder()
                    .name("General Retail APMC Market Hub")
                    .code("MKT-DEFAULT-" + System.currentTimeMillis())
                    .city("Central Retail Zone")
                    .district(defaultDistrict)
                    .address("Central Retail Zone")
                    .build());
        }

        Commodity commodity = null;
        if (request.getCommodityId() != null && request.getCommodityId() > 0) {
            commodity = commodityRepository.findById(request.getCommodityId()).orElse(null);
        }

        Complaint complaint = Complaint.builder()
                .user(user)
                .market(market)
                .commodity(commodity)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ComplaintStatus.PENDING)
                .build();

        return mapToResponse(complaintRepository.save(complaint));
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaintStatus(Long complaintId, ComplaintStatus status,
                                                   String resolutionNotes, String authorityUsername) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        User authority = userRepository.findByUsernameOrEmail(authorityUsername, authorityUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authorityUsername));

        complaint.setStatus(status);
        complaint.setResolutionNotes(resolutionNotes);
        complaint.setAssignedAuthority(authority);

        return mapToResponse(complaintRepository.save(complaint));
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        return mapToResponse(complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id, String principalName, boolean isPrivileged) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));
        if (!isPrivileged) {
            String ownerUsername = complaint.getUser().getUsername();
            String ownerEmail = complaint.getUser().getEmail();
            if (!ownerUsername.equals(principalName) && !ownerEmail.equals(principalName)) {
                throw new AccessDeniedException("You are not authorized to view this complaint.");
            }
        }
        return mapToResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> getUserComplaints(String username, int page, int size) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Complaint> complaintsPage = complaintRepository.findByUserId(user.getId(), pageable);
        return toPagedResponse(complaintsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> getComplaintsByStatus(ComplaintStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Complaint> complaintsPage = complaintRepository.findByStatus(status, pageable);
        return toPagedResponse(complaintsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ComplaintResponse> getAllComplaints(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Complaint> complaintsPage = complaintRepository.findAll(pageable);
        return toPagedResponse(complaintsPage);
    }


    private PagedResponse<ComplaintResponse> toPagedResponse(Page<Complaint> page) {
        List<ComplaintResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ComplaintResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private ComplaintResponse mapToResponse(Complaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .userId(c.getUser().getId())
                .username(c.getUser().getUsername())
                .email(c.getUser().getEmail())
                .marketId(c.getMarket().getId())
                .marketName(c.getMarket().getName())
                .commodityId(c.getCommodity() != null ? c.getCommodity().getId() : null)
                .commodityName(c.getCommodity() != null ? c.getCommodity().getName() : null)
                .title(c.getTitle())
                .description(c.getDescription())
                .status(c.getStatus())
                .assignedAuthorityUsername(c.getAssignedAuthority() != null ? c.getAssignedAuthority().getUsername() : null)
                .resolutionNotes(c.getResolutionNotes())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
