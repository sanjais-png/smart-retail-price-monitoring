package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketResponse {
    private Long id;
    private String name;
    private String code;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
    private Long districtId;
    private String districtName;
    private Long stateId;
    private String stateName;
}
