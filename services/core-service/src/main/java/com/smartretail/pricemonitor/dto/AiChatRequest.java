package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {
    private String query;
    private String mode;
    private String activeLocation;
    private String activeState;
    private List<Map<String, String>> history;
}
