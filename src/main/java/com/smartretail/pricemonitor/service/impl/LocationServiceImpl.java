package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.*;
import com.smartretail.pricemonitor.entity.District;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.State;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.DistrictRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.StateRepository;
import com.smartretail.pricemonitor.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final MarketRepository marketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StateResponse> getAllStates() {
        return stateRepository.findAll().stream()
                .map(s -> StateResponse.builder().id(s.getId()).name(s.getName()).code(s.getCode()).build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistrictResponse> getDistrictsByState(Long stateId) {
        return districtRepository.findByStateId(stateId).stream()
                .map(d -> DistrictResponse.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .stateId(d.getState().getId())
                        .stateName(d.getState().getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketResponse> getMarketsByDistrict(Long districtId) {
        return marketRepository.findByDistrictId(districtId).stream()
                .map(this::mapToMarketResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketResponse> getMarketsByCity(String city) {
        return marketRepository.findByCityIgnoreCase(city).stream()
                .map(this::mapToMarketResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MarketResponse> searchMarkets(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Market> marketsPage = marketRepository.searchMarkets(query, pageable);

        List<MarketResponse> content = marketsPage.getContent().stream()
                .map(this::mapToMarketResponse)
                .collect(Collectors.toList());

        return PagedResponse.<MarketResponse>builder()
                .content(content)
                .page(marketsPage.getNumber())
                .size(marketsPage.getSize())
                .totalElements(marketsPage.getTotalElements())
                .totalPages(marketsPage.getTotalPages())
                .last(marketsPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MarketResponse getMarketById(Long id) {
        Market market = marketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Market", "id", id));
        return mapToMarketResponse(market);
    }

    @Override
    @Transactional
    public MarketResponse createMarket(MarketRequest request) {
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", request.getDistrictId()));

        Market market = Market.builder()
                .name(request.getName())
                .code(request.getCode())
                .city(request.getCity())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .district(district)
                .build();

        return mapToMarketResponse(marketRepository.save(market));
    }

    @Override
    @Transactional
    public MarketBulkImportDto.BulkImportResponse bulkImportMarkets(List<MarketBulkImportDto> requests) {
        int created = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (MarketBulkImportDto dto : requests) {
            try {
                MarketResponse m = findOrCreateMarketByLocation(
                        dto.getMarketName(), dto.getCity(), dto.getStateName(), dto.getLatitude(), dto.getLongitude()
                );
                created++;
            } catch (Exception e) {
                skipped++;
                errors.add("Failed for " + dto.getMarketName() + ": " + e.getMessage());
            }
        }

        return MarketBulkImportDto.BulkImportResponse.builder()
                .totalProcessed(requests.size())
                .createdCount(created)
                .skippedCount(skipped)
                .errorMessages(errors)
                .build();
    }

    @Override
    @Transactional
    public MarketResponse findOrCreateMarketByLocation(String marketName, String cityName, String stateName, Double latitude, Double longitude) {
        // 1. Find or create State
        String finalStateName = (stateName != null && !stateName.isBlank()) ? stateName : "Default State";
        State state = stateRepository.findByName(finalStateName).orElseGet(() ->
                stateRepository.save(State.builder().name(finalStateName).code(finalStateName.substring(0, Math.min(3, finalStateName.length())).toUpperCase()).build())
        );

        // 2. Find or create District
        String finalCityName = (cityName != null && !cityName.isBlank()) ? cityName : "Central District";
        District district = districtRepository.findByNameAndStateId(finalCityName, state.getId()).orElseGet(() ->
                districtRepository.save(District.builder().name(finalCityName).state(state).build())
        );

        // 3. Find or create Market
        Optional<Market> existing = marketRepository.findByCityIgnoreCase(finalCityName).stream()
                .filter(m -> m.getName().equalsIgnoreCase(marketName))
                .findFirst();

        if (existing.isPresent()) {
            return mapToMarketResponse(existing.get());
        }

        String marketCode = "MKT_" + marketName.replaceAll("[^a-zA-Z0-9]", "_").toUpperCase();
        if (marketCode.length() > 30) marketCode = marketCode.substring(0, 30);

        Market newMarket = Market.builder()
                .name(marketName)
                .code(marketCode)
                .city(finalCityName)
                .latitude(latitude != null ? latitude : 0.0)
                .longitude(longitude != null ? longitude : 0.0)
                .district(district)
                .build();

        return mapToMarketResponse(marketRepository.save(newMarket));
    }

    private MarketResponse mapToMarketResponse(Market market) {
        return MarketResponse.builder()
                .id(market.getId())
                .name(market.getName())
                .code(market.getCode())
                .city(market.getCity())
                .address(market.getAddress())
                .latitude(market.getLatitude())
                .longitude(market.getLongitude())
                .districtId(market.getDistrict().getId())
                .districtName(market.getDistrict().getName())
                .stateId(market.getDistrict().getState().getId())
                .stateName(market.getDistrict().getState().getName())
                .build();
    }
}
