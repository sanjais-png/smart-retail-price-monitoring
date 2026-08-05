package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.dto.UserProfileUpdateRequest;
import com.smartretail.pricemonitor.dto.UserResponse;

public interface UserService {
    UserResponse getCurrentUserProfile(String username);
    UserResponse updateProfile(String username, UserProfileUpdateRequest request);
    UserResponse getUserById(Long id);
    PagedResponse<UserResponse> getAllUsers(int page, int size);
    UserResponse toggleUserStatus(Long id, boolean enabled);
}
