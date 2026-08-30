package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.*;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    JwtAuthResponse login(LoginRequest request);
    JwtAuthResponse refreshToken(RefreshTokenRequest request);
    boolean verifyOtp(VerifyOtpRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
