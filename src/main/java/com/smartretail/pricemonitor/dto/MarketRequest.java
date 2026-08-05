package com.smartretail.pricemonitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MarketRequest {
    @NotBlank(message = "Market name is required")
    private String name;

    @NotBlank(message = "Market code is required")
    private String code;

    @NotBlank(message = "City is required")
    private String city;

    private String address;
    private Double latitude;
    private Double longitude;

    @NotNull(message = "District ID is required")
    private Long districtId;
}
