package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.CommodityCategoryResponse;
import com.smartretail.pricemonitor.entity.CommodityCategory;
import com.smartretail.pricemonitor.exception.DuplicateResourceException;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.CommodityCategoryRepository;
import com.smartretail.pricemonitor.service.CommodityCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommodityCategoryServiceImpl implements CommodityCategoryService {

    private final CommodityCategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommodityCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommodityCategoryResponse getCategoryById(Long id) {
        CommodityCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommodityCategory", "id", id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CommodityCategoryResponse createCategory(String name, String description) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category name already exists: " + name);
        }
        CommodityCategory category = CommodityCategory.builder()
                .name(name)
                .description(description)
                .build();
        return mapToResponse(categoryRepository.save(category));
    }

    private CommodityCategoryResponse mapToResponse(CommodityCategory category) {
        return CommodityCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
