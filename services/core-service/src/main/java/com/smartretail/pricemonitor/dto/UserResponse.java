package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private Double latitude;
    private Double longitude;
    private boolean enabled;
    private boolean isEmailVerified;
    private boolean isOtpVerified;
    private Set<String> roles;
    private String approvalStatus;
    private LocalDateTime createdAt;
}
