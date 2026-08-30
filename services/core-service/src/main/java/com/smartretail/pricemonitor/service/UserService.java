package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.ChangePasswordRequest;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.dto.UserProfileUpdateRequest;
import com.smartretail.pricemonitor.dto.UserResponse;
import com.smartretail.pricemonitor.dto.VerifyPasswordRequest;

public interface UserService {
    UserResponse getCurrentUserProfile(String username);
    UserResponse updateProfile(String username, UserProfileUpdateRequest request);
    boolean verifyCurrentPassword(String username, VerifyPasswordRequest request);
    void changePassword(String username, ChangePasswordRequest request);
    UserResponse getUserById(Long id);
    PagedResponse<UserResponse> getAllUsers(int page, int size);
    UserResponse toggleUserStatus(Long id, boolean enabled);
}
