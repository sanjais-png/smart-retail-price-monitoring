package com.smartretail.pricemonitor.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateRequest {
    private String firstName;
    private String lastName;

    @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number starting with 6, 7, 8, or 9.")
    private String phone;

    private String address;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees.")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees.")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180 degrees.")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180 degrees.")
    private Double longitude;
}
