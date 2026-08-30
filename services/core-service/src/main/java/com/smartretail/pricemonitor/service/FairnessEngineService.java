package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.FairnessCheckRequest;
import com.smartretail.pricemonitor.dto.FairnessCheckResponse;

public interface FairnessEngineService {
    FairnessCheckResponse evaluateFairness(String username, FairnessCheckRequest request);
}
