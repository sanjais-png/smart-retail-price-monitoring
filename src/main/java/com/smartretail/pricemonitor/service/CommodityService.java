package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.CommodityRequest;
import com.smartretail.pricemonitor.dto.CommodityResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;

public interface CommodityService {
    CommodityResponse createCommodity(CommodityRequest request);
    CommodityResponse updateCommodity(Long id, CommodityRequest request);
    CommodityResponse getCommodityById(Long id);
    CommodityResponse getCommodityByCode(String code);
    PagedResponse<CommodityResponse> getAllCommodities(int page, int size, String sortBy, String sortDir);
    PagedResponse<CommodityResponse> searchCommodities(String query, int page, int size);
    PagedResponse<CommodityResponse> getCommoditiesByCategory(Long categoryId, int page, int size);
    void deleteCommodity(Long id);
}
