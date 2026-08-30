package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.*;

import java.util.List;

public interface LocationService {
    List<StateResponse> getAllStates();
    List<DistrictResponse> getDistrictsByState(Long stateId);
    List<DistrictResponse> getDistrictsByStateName(String stateName);
    List<MarketResponse> getMarketsByDistrict(Long districtId);
    List<MarketResponse> getMarketsByCity(String city);
    List<MarketResponse> getMarketsByStateAndDistrict(String stateName, String districtName);
    PagedResponse<MarketResponse> searchMarkets(String query, int page, int size);
    MarketResponse getMarketById(Long id);
    MarketResponse createMarket(MarketRequest request);
    MarketBulkImportDto.BulkImportResponse bulkImportMarkets(List<MarketBulkImportDto> requests);
    MarketResponse findOrCreateMarketByLocation(String marketName, String cityName, String stateName, Double latitude, Double longitude);
}
