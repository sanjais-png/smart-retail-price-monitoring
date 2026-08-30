package com.smartretail.pricemonitor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;
}
