package com.smartretail.pricemonitor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "OTP code is required")
    private String otpCode;
}
