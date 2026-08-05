package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.CommodityCategoryResponse;

import java.util.List;

public interface CommodityCategoryService {
    List<CommodityCategoryResponse> getAllCategories();
    CommodityCategoryResponse getCategoryById(Long id);
    CommodityCategoryResponse createCategory(String name, String description);
}
