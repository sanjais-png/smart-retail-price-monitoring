package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.AlertRequest;
import com.smartretail.pricemonitor.dto.AlertResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.entity.Alert;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.User;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.exception.UnauthorizedException;
import com.smartretail.pricemonitor.repository.AlertRepository;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.UserRepository;
import com.smartretail.pricemonitor.service.AlertService;
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
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;

    @Override
    @Transactional
    public AlertResponse createAlert(String username, AlertRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Commodity commodity = commodityRepository.findById(request.getCommodityId())
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", request.getCommodityId()));

        Market market = marketRepository.findById(request.getMarketId())
                .orElseThrow(() -> new ResourceNotFoundException("Market", "id", request.getMarketId()));

        Alert alert = Alert.builder()
                .user(user)
                .commodity(commodity)
                .market(market)
                .alertType(request.getAlertType())
                .targetPrice(request.getTargetPrice())
                .isActive(true)
                .build();

        return mapToResponse(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public AlertResponse toggleAlert(String username, Long alertId, boolean active) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));

        if (!alert.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("You are not authorized to modify this alert");
        }

        alert.setActive(active);
        return mapToResponse(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public void deleteAlert(String username, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));

        if (!alert.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("You are not authorized to delete this alert");
        }

        alertRepository.delete(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AlertResponse> getUserAlerts(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Alert> alertsPage = alertRepository.findByUserId(user.getId(), pageable);

        List<AlertResponse> content = alertsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<AlertResponse>builder()
                .content(content)
                .page(alertsPage.getNumber())
                .size(alertsPage.getSize())
                .totalElements(alertsPage.getTotalElements())
                .totalPages(alertsPage.getTotalPages())
                .last(alertsPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertResponse getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", id));
        return mapToResponse(alert);
    }

    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .commodityId(alert.getCommodity().getId())
                .commodityName(alert.getCommodity().getName())
                .marketId(alert.getMarket().getId())
                .marketName(alert.getMarket().getName())
                .alertType(alert.getAlertType())
                .targetPrice(alert.getTargetPrice())
                .active(alert.isActive())
                .lastEvaluatedPrice(alert.getLastEvaluatedPrice())
                .lastEvaluatedAt(alert.getLastEvaluatedAt())
                .lastTriggeredPrice(alert.getLastTriggeredPrice())
                .lastTriggeredAt(alert.getLastTriggeredAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
