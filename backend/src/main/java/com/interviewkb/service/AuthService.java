package com.interviewkb.service;

import com.interviewkb.dto.request.*;
import com.interviewkb.dto.response.LoginResponse;
import com.interviewkb.dto.response.WechatQrcodeResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    LoginResponse refreshToken(String refreshToken);

    void logout(String token);

    void sendPasswordResetEmail(String email);

    void resetPassword(String token, String newPassword);

    void changePassword(Long userId, String oldPassword, String newPassword);

    // 手机验证码登录
    void sendSmsCode(SmsCodeRequest request);

    LoginResponse loginByPhone(PhoneLoginRequest request);

    // 微信登录
    WechatQrcodeResponse getWechatQrcode();

    LoginResponse loginByWechat(WechatLoginRequest request);

    LoginResponse checkWechatLoginStatus(String sceneStr);

    // 绑定功能
    void bindPhone(Long userId, BindPhoneRequest request);

    void bindEmail(Long userId, BindEmailRequest request);

    void bindWechat(Long userId, String code);

    void unbindWechat(Long userId);
}
