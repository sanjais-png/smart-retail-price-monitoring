package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.constants.RoleName;
import com.smartretail.pricemonitor.dto.*;
import com.smartretail.pricemonitor.entity.Role;
import com.smartretail.pricemonitor.entity.User;
import com.smartretail.pricemonitor.exception.BadRequestException;
import com.smartretail.pricemonitor.exception.DuplicateResourceException;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.notification.EmailService;
import com.smartretail.pricemonitor.repository.RoleRepository;
import com.smartretail.pricemonitor.repository.UserRepository;
import com.smartretail.pricemonitor.security.JwtTokenProvider;
import com.smartretail.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered!");
        }

        // SECURITY: Public registration ALWAYS assigns ROLE_USER only.
        // Any client-supplied roles field is intentionally ignored.
        // Privileged accounts (ROLE_AUTHORITY, ROLE_ADMIN) must be provisioned
        // through a trusted administrative process — never via this public endpoint.
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_USER"));
        roles.add(userRole);

        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);

        com.smartretail.pricemonitor.constants.ApprovalStatus status = request.isRequestAuthorityAccess()
                ? com.smartretail.pricemonitor.constants.ApprovalStatus.PENDING_AUTHORITY
                : com.smartretail.pricemonitor.constants.ApprovalStatus.ACTIVE;

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .enabled(true)
                .isEmailVerified(false)
                .isOtpVerified(false)
                .approvalStatus(status)
                .otpCode(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(15))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        emailService.sendEmail(
                savedUser.getEmail(),
                "Smart Retail Platform - Verification OTP",
                "Welcome " + savedUser.getUsername() + ",\n\nYour OTP for account verification is: " + otp
        );

        return mapToUserResponse(savedUser);
    }

    @Override
    public JwtAuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication.getName());

        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .approvalStatus(user.getApprovalStatus() != null ? user.getApprovalStatus().name() : "ACTIVE")
                .build();
    }

    @Override
    public JwtAuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new BadRequestException("Invalid or expired refresh token!");
        }

        String username = tokenProvider.getUsernameFromToken(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, 
                roles.stream().map(r -> (GrantedAuthority) () -> r).collect(Collectors.toList()));

        String newAccessToken = tokenProvider.generateAccessToken(auth);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        return JwtAuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public boolean verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            throw new BadRequestException("Invalid OTP code!");
        }
        if (user.getOtpExpiry() != null && user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP code has expired!");
        }

        user.setOtpVerified(true);
        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return true;
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Smart Retail Platform - Password Reset OTP",
                "Your password reset OTP is: " + otp + "\nValid for 15 minutes."
        );
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            throw new BadRequestException("Invalid OTP code!");
        }
        if (user.getOtpExpiry() != null && user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP code has expired!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .isEmailVerified(user.isEmailVerified())
                .isOtpVerified(user.isOtpVerified())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .approvalStatus(user.getApprovalStatus() != null ? user.getApprovalStatus().name() : "ACTIVE")
                .createdAt(user.getCreatedAt())
                .build();
    }
}
