package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.constants.ApprovalStatus;
import com.smartretail.pricemonitor.constants.RoleName;
import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.dto.SystemSettingDto;
import com.smartretail.pricemonitor.dto.UserResponse;
import com.smartretail.pricemonitor.entity.SystemSetting;
import com.smartretail.pricemonitor.entity.User;
import com.smartretail.pricemonitor.exception.BadRequestException;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.repository.ComplaintRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.RoleRepository;
import com.smartretail.pricemonitor.repository.SystemSettingRepository;
import com.smartretail.pricemonitor.repository.UserRepository;
import com.smartretail.pricemonitor.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Platform administration, authority request management, user management, and system settings")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ComplaintRepository complaintRepository;
    private final MarketRepository marketRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogService auditLogService;

    @GetMapping("/dashboard-stats")
    @Operation(summary = "Get platform metrics for Admin overview dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("authorityUsers", userRepository.countByRoles_Name(RoleName.ROLE_AUTHORITY));
        stats.put("pendingAuthorityRequests", userRepository.countByApprovalStatus(ApprovalStatus.PENDING_AUTHORITY));
        stats.put("totalComplaints", complaintRepository.count());
        stats.put("totalMarkets", marketRepository.count());
        return ResponseEntity.ok(ApiResponse.success("Platform dashboard stats retrieved", stats));
    }

    @GetMapping("/authority-requests")
    @Operation(summary = "Get all pending Authority access requests")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAuthorityRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> usersPage = userRepository.findByApprovalStatus(ApprovalStatus.PENDING_AUTHORITY, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending authority requests retrieved", toPagedResponse(usersPage)));
    }

    @PostMapping("/authority-requests/{userId}/approve")
    @Transactional
    @Operation(summary = "Approve an Authority access request (Atomic role replacement & audit log)")
    public ResponseEntity<ApiResponse<UserResponse>> approveAuthorityRequest(
            Authentication authentication,
            @PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // State validation for idempotency
        if (user.getApprovalStatus() != ApprovalStatus.PENDING_AUTHORITY) {
            throw new BadRequestException("User " + user.getUsername() + " is not in PENDING_AUTHORITY status (current: " + user.getApprovalStatus() + ")");
        }

        // Role replacement: replace ROLE_USER with ROLE_AUTHORITY
        com.smartretail.pricemonitor.entity.Role authorityRole = roleRepository.findByName(RoleName.ROLE_AUTHORITY)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_AUTHORITY"));

        Set<com.smartretail.pricemonitor.entity.Role> newRoles = new HashSet<>();
        newRoles.add(authorityRole);

        user.setRoles(newRoles);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setEnabled(true);
        User updated = userRepository.save(user);

        // Transactional Audit Log
        auditLogService.log(authentication.getName(), "APPROVE_AUTHORITY", "User", userId,
                "Approved Authority privileges for user: " + user.getUsername() + " (role upgraded to ROLE_AUTHORITY)");

        return ResponseEntity.ok(ApiResponse.success("Authority request approved for " + user.getUsername(), mapToUserResponse(updated)));
    }

    @PostMapping("/authority-requests/{userId}/deny")
    @Transactional
    @Operation(summary = "Deny an Authority access request (Atomic status update & audit log)")
    public ResponseEntity<ApiResponse<UserResponse>> denyAuthorityRequest(
            Authentication authentication,
            @PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // State validation for idempotency
        if (user.getApprovalStatus() != ApprovalStatus.PENDING_AUTHORITY) {
            throw new BadRequestException("User " + user.getUsername() + " is not in PENDING_AUTHORITY status (current: " + user.getApprovalStatus() + ")");
        }

        user.setApprovalStatus(ApprovalStatus.DENIED);
        User updated = userRepository.save(user);

        // Transactional Audit Log
        auditLogService.log(authentication.getName(), "DENY_AUTHORITY", "User", userId,
                "Denied Authority privileges for user: " + user.getUsername() + " (remains ROLE_USER)");

        return ResponseEntity.ok(ApiResponse.success("Authority request denied for " + user.getUsername(), mapToUserResponse(updated)));
    }

    @GetMapping("/users/by-role")
    @Operation(summary = "Get users filtered by role (e.g. ROLE_AUTHORITY or ROLE_USER)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsersByRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        RoleName roleName;
        try {
            roleName = RoleName.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role specified: " + role);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> usersPage = userRepository.findByRoles_Name(roleName, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users with role " + roleName + " retrieved", toPagedResponse(usersPage)));
    }

    @GetMapping("/settings")
    @Operation(summary = "Get all platform system settings (MySQL backed)")
    public ResponseEntity<ApiResponse<List<SystemSettingDto>>> getSystemSettings() {
        initDefaultSettingsIfEmpty();
        List<SystemSettingDto> settings = systemSettingRepository.findAll().stream()
                .map(this::mapToSettingDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("System settings retrieved", settings));
    }

    @PutMapping("/settings/{key}")
    @Transactional
    @Operation(summary = "Update a system setting value")
    public ResponseEntity<ApiResponse<SystemSettingDto>> updateSystemSetting(
            Authentication authentication,
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        String newValue = body.get("settingValue");
        if (newValue == null) {
            throw new BadRequestException("settingValue field is required");
        }

        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        if (!setting.getIsEditable()) {
            throw new BadRequestException("Setting '" + key + "' is read-only and cannot be modified");
        }

        String oldValue = setting.getSettingValue();
        setting.setSettingValue(newValue);
        setting.setUpdatedBy(authentication.getName());
        SystemSetting updated = systemSettingRepository.save(setting);

        auditLogService.log(authentication.getName(), "UPDATE_SETTING", "SystemSetting", setting.getId(),
                "Updated setting '" + key + "' from '" + oldValue + "' to '" + newValue + "'");

        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", mapToSettingDto(updated)));
    }

    private void initDefaultSettingsIfEmpty() {
        if (systemSettingRepository.count() == 0) {
            systemSettingRepository.save(SystemSetting.builder()
                    .settingKey("max_receipt_upload_size_mb")
                    .settingValue("10")
                    .description("Maximum allowed receipt upload file size in megabytes")
                    .isEditable(true)
                    .updatedBy("SYSTEM")
                    .build());

            systemSettingRepository.save(SystemSetting.builder()
                    .settingKey("auto_close_inactive_days")
                    .settingValue("30")
                    .description("Days after which inactive pending complaints are auto-closed")
                    .isEditable(true)
                    .updatedBy("SYSTEM")
                    .build());

            systemSettingRepository.save(SystemSetting.builder()
                    .settingKey("audit_log_retention_days")
                    .settingValue("90")
                    .description("Days to retain detailed compliance audit logs before archiving")
                    .isEditable(true)
                    .updatedBy("SYSTEM")
                    .build());

            systemSettingRepository.save(SystemSetting.builder()
                    .settingKey("agmarknet_sync_frequency")
                    .settingValue("Daily at 06:00 AM IST")
                    .description("Agmarknet government mandi data automated ingestion schedule (Read-only)")
                    .isEditable(false)
                    .updatedBy("SYSTEM")
                    .build());
        }
    }

    private PagedResponse<UserResponse> toPagedResponse(Page<User> page) {
        List<UserResponse> content = page.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
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

    private SystemSettingDto mapToSettingDto(SystemSetting setting) {
        return SystemSettingDto.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .description(setting.getDescription())
                .isEditable(setting.getIsEditable())
                .updatedAt(setting.getUpdatedAt())
                .updatedBy(setting.getUpdatedBy())
                .build();
    }
}
