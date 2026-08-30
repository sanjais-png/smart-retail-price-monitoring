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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final MarketRepository marketRepository;

    private static final Map<String, double[]> CITY_COORDINATES = Map.ofEntries(
        Map.entry("coimbatore", new double[]{11.0168, 76.9558}),
        Map.entry("chennai", new double[]{13.0827, 80.2707}),
        Map.entry("madurai", new double[]{9.9252, 78.1198}),
        Map.entry("salem", new double[]{11.6643, 78.1460}),
        Map.entry("tiruchirappalli", new double[]{10.7905, 78.7047}),
        Map.entry("trichy", new double[]{10.7905, 78.7047}),
        Map.entry("erode", new double[]{11.3410, 77.7172}),
        Map.entry("tiruppur", new double[]{11.1085, 77.3411}),
        Map.entry("vellore", new double[]{12.9165, 79.1325}),
        Map.entry("mumbai", new double[]{19.0760, 72.8777}),
        Map.entry("pune", new double[]{18.5204, 73.8567}),
        Map.entry("nashik", new double[]{19.9975, 73.7898}),
        Map.entry("delhi", new double[]{28.6139, 77.2090}),
        Map.entry("new delhi", new double[]{28.6139, 77.2090}),
        Map.entry("bengaluru", new double[]{12.9716, 77.5946}),
        Map.entry("bangalore", new double[]{12.9716, 77.5946}),
        Map.entry("hyderabad", new double[]{17.3850, 78.4867}),
        Map.entry("thiruvananthapuram", new double[]{8.5241, 76.9366}),
        Map.entry("kochi", new double[]{9.9312, 76.2673})
    );

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
    public List<DistrictResponse> getDistrictsByStateName(String stateName) {
        if (stateName == null || stateName.isBlank()) {
            return districtRepository.findAll().stream()
                    .map(d -> DistrictResponse.builder()
                            .id(d.getId())
                            .name(d.getName())
                            .stateId(d.getState() != null ? d.getState().getId() : null)
                            .stateName(d.getState() != null ? d.getState().getName() : "")
                            .build())
                    .collect(Collectors.toList());
        }
        return districtRepository.findAll().stream()
                .filter(d -> d.getState() != null && d.getState().getName().equalsIgnoreCase(stateName.trim()))
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
    public List<MarketResponse> getMarketsByStateAndDistrict(String stateName, String districtName) {
        List<Market> allMarkets = marketRepository.findAll();
        return allMarkets.stream()
                .filter(m -> {
                    boolean stateMatch = (stateName == null || stateName.isBlank()) ||
                            (m.getDistrict() != null && m.getDistrict().getState() != null &&
                             m.getDistrict().getState().getName().equalsIgnoreCase(stateName.trim()));
                    boolean distMatch = (districtName == null || districtName.isBlank()) ||
                            (m.getDistrict() != null && m.getDistrict().getName().equalsIgnoreCase(districtName.trim()));
                    return stateMatch && distMatch;
                })
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

        double[] coords = resolveCoordinates(request.getCity(), request.getLatitude(), request.getLongitude());

        Market market = Market.builder()
                .name(request.getName())
                .code(request.getCode())
                .city(request.getCity())
                .address(request.getAddress())
                .latitude(coords[0])
                .longitude(coords[1])
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
                findOrCreateMarketByLocation(
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
        String finalStateName = (stateName != null && !stateName.isBlank()) ? stateName : "Default State";
        State state = stateRepository.findByName(finalStateName).orElseGet(() ->
                stateRepository.save(State.builder().name(finalStateName).code(finalStateName.substring(0, Math.min(3, finalStateName.length())).toUpperCase()).build())
        );

        String finalCityName = (cityName != null && !cityName.isBlank()) ? cityName : "Central District";
        District district = districtRepository.findByNameAndStateId(finalCityName, state.getId()).orElseGet(() ->
                districtRepository.save(District.builder().name(finalCityName).state(state).build())
        );

        // 1. Fuzzy match existing market by name or city
        Optional<Market> existing = marketRepository.findByCityIgnoreCase(finalCityName).stream()
                .filter(m -> m.getName().equalsIgnoreCase(marketName) || m.getName().toLowerCase().contains(marketName.toLowerCase()))
                .findFirst();

        if (existing.isPresent()) {
            return mapToMarketResponse(existing.get());
        }

        // 2. Generate Guaranteed Unique Market Code
        String baseCode = "MKT_" + marketName.replaceAll("[^a-zA-Z0-9]", "_").toUpperCase();
        if (baseCode.length() > 20) baseCode = baseCode.substring(0, 20);
        String uniqueCode = baseCode + "_" + (System.currentTimeMillis() % 10000);

        double[] coords = resolveCoordinates(finalCityName, latitude, longitude);

        Market newMarket = Market.builder()
                .name(marketName)
                .code(uniqueCode)
                .city(finalCityName)
                .latitude(coords[0])
                .longitude(coords[1])
                .district(district)
                .build();

        return mapToMarketResponse(marketRepository.save(newMarket));
    }

    private double[] resolveCoordinates(String cityName, Double inputLat, Double inputLon) {
        if (inputLat != null && inputLat != 0.0 && inputLon != null && inputLon != 0.0) {
            return new double[]{inputLat, inputLon};
        }

        if (cityName != null) {
            String key = cityName.trim().toLowerCase();
            if (CITY_COORDINATES.containsKey(key)) {
                return CITY_COORDINATES.get(key);
            }
        }
        return new double[]{13.0827, 80.2707}; // Default Regional Center (Chennai, TN)
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
