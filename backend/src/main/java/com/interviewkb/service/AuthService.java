package com.interviewkb.service;

import com.interviewkb.dto.request.LoginRequest;
import com.interviewkb.dto.request.RegisterRequest;
import com.interviewkb.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    LoginResponse refreshToken(String refreshToken);

    void logout(String token);

    void sendPasswordResetEmail(String email);

    void resetPassword(String token, String newPassword);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
