package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.CommodityRequest;
import com.smartretail.pricemonitor.dto.CommodityResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.CommodityCategory;
import com.smartretail.pricemonitor.exception.DuplicateResourceException;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.CommodityCategoryRepository;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.service.CommodityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommodityServiceImpl implements CommodityService {

    private final CommodityRepository commodityRepository;
    private final CommodityCategoryRepository categoryRepository;

    @Override
    @Transactional
    public CommodityResponse createCommodity(CommodityRequest request) {
        if (commodityRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("Commodity with code '" + request.getCode() + "' already exists!");
        }

        CommodityCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CommodityCategory", "id", request.getCategoryId()));

        Commodity commodity = Commodity.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .unit(request.getUnit())
                .category(category)
                .imagePath(request.getImagePath())
                .isActive(true)
                .build();

        return mapToResponse(commodityRepository.save(commodity));
    }

    @Override
    @Transactional
    public CommodityResponse updateCommodity(Long id, CommodityRequest request) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", id));

        CommodityCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CommodityCategory", "id", request.getCategoryId()));

        commodity.setName(request.getName());
        commodity.setCode(request.getCode());
        commodity.setDescription(request.getDescription());
        commodity.setUnit(request.getUnit());
        commodity.setCategory(category);
        if (request.getImagePath() != null) {
            commodity.setImagePath(request.getImagePath());
        }

        return mapToResponse(commodityRepository.save(commodity));
    }

    @Override
    @Transactional(readOnly = true)
    public CommodityResponse getCommodityById(Long id) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", id));
        return mapToResponse(commodity);
    }

    @Override
    @Transactional(readOnly = true)
    public CommodityResponse getCommodityByCode(String code) {
        Commodity commodity = commodityRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "code", code));
        return mapToResponse(commodity);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommodityResponse> getAllCommodities(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Commodity> commoditiesPage = commodityRepository.findAll(pageable);
        return mapToPagedResponse(commoditiesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommodityResponse> searchCommodities(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Commodity> commoditiesPage = commodityRepository.searchCommodities(query, pageable);
        return mapToPagedResponse(commoditiesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommodityResponse> getCommoditiesByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Commodity> commoditiesPage = commodityRepository.findByCategoryId(categoryId, pageable);
        return mapToPagedResponse(commoditiesPage);
    }

    @Override
    @Transactional
    public void deleteCommodity(Long id) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", id));
        commodity.setActive(false); // soft delete
        commodityRepository.save(commodity);
    }

    private CommodityResponse mapToResponse(Commodity commodity) {
        return CommodityResponse.builder()
                .id(commodity.getId())
                .name(commodity.getName())
                .code(commodity.getCode())
                .description(commodity.getDescription())
                .unit(commodity.getUnit())
                .imagePath(commodity.getImagePath())
                .categoryId(commodity.getCategory().getId())
                .categoryName(commodity.getCategory().getName())
                .active(commodity.isActive())
                .createdAt(commodity.getCreatedAt())
                .build();
    }

    private PagedResponse<CommodityResponse> mapToPagedResponse(Page<Commodity> page) {
        List<CommodityResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<CommodityResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
