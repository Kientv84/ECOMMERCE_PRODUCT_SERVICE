package com.ecommerce.kientv84.services;

import com.ecommerce.kientv84.dtos.request.ChangePasswordRequest;
import com.ecommerce.kientv84.dtos.request.LoginRequest;
import com.ecommerce.kientv84.dtos.request.ResetPasswordRequest;
import com.ecommerce.kientv84.dtos.request.TokenRefreshRequest;
import com.ecommerce.kientv84.dtos.response.ChangePasswordResponse;
import com.ecommerce.kientv84.dtos.response.LoginResponse;
import com.ecommerce.kientv84.dtos.response.TokenRefreshResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request) ;

    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    ChangePasswordResponse changePassword(ChangePasswordRequest request);

    void sendResetPasswordCode(String email);

    void resetPassword(ResetPasswordRequest request);

    void sendEmail(String to, String subject, String content);
}
