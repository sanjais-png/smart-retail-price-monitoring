package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.AlertRequest;
import com.smartretail.pricemonitor.dto.AlertResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;

public interface AlertService {
    AlertResponse createAlert(String username, AlertRequest request);
    AlertResponse toggleAlert(String username, Long alertId, boolean active);
    void deleteAlert(String username, Long alertId);
    PagedResponse<AlertResponse> getUserAlerts(String username, int page, int size);
    AlertResponse getAlertById(Long id);
}
