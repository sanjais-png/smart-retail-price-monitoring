package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.*;

import java.util.List;

public interface LocationService {
    List<StateResponse> getAllStates();
    List<DistrictResponse> getDistrictsByState(Long stateId);
    List<MarketResponse> getMarketsByDistrict(Long districtId);
    List<MarketResponse> getMarketsByCity(String city);
    PagedResponse<MarketResponse> searchMarkets(String query, int page, int size);
    MarketResponse getMarketById(Long id);
    MarketResponse createMarket(MarketRequest request);
    MarketBulkImportDto.BulkImportResponse bulkImportMarkets(List<MarketBulkImportDto> requests);
    MarketResponse findOrCreateMarketByLocation(String marketName, String cityName, String stateName, Double latitude, Double longitude);
}
