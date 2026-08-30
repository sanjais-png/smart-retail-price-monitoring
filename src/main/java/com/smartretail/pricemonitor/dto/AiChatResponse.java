package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String reply;
    private String detectedCommodity;
    private String detectedLocation;
    private Double calculatedFairnessScore;
    private Boolean isGougingDetected;
    private String actionRecommendation;
    private String source; // "AI", "GUIDED", "UNAVAILABLE"
    private String groundingStatus; // "GROUNDED_IN_DB", "GENERAL_GUIDANCE", "AI_UNAVAILABLE"
    private String friendlyToolStatus; // e.g. "Checking current market prices..."
}
