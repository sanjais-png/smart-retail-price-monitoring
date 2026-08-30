package com.smartretail.pricemonitor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Builder.Default
    @Column(name = "is_enabled", nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = true;

    @Builder.Default
    @Column(name = "is_otp_verified", nullable = false)
    private Boolean isOtpVerified = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    private com.smartretail.pricemonitor.constants.ApprovalStatus approvalStatus = com.smartretail.pricemonitor.constants.ApprovalStatus.ACTIVE;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(isEmailVerified);
    }

    public boolean isOtpVerified() {
        return Boolean.TRUE.equals(isOtpVerified);
    }

    public void setEmailVerified(boolean emailVerified) {
        this.isEmailVerified = emailVerified;
    }

    public void setOtpVerified(boolean otpVerified) {
        this.isOtpVerified = otpVerified;
    }

    @Column(name = "otp_code", length = 10)
    private String otpCode;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
