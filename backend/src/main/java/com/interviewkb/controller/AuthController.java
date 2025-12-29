package com.interviewkb.controller;

import com.interviewkb.common.result.Result;
import com.interviewkb.dto.request.*;
import com.interviewkb.dto.response.LoginResponse;
import com.interviewkb.dto.response.WechatQrcodeResponse;
import com.interviewkb.security.CurrentUser;
import com.interviewkb.security.UserPrincipal;
import com.interviewkb.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户登录、注册、密码管理等")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        authService.logout(token);
        return Result.success();
    }

    @Operation(summary = "发送密码重置邮件")
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@RequestParam String email) {
        authService.sendPasswordResetEmail(email);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return Result.success();
    }

    // ====================== 手机验证码登录 ======================

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        authService.sendSmsCode(request);
        return Result.success("验证码已发送");
    }

    @Operation(summary = "手机验证码登录")
    @PostMapping("/login/phone")
    public Result<LoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return Result.success(authService.loginByPhone(request));
    }

    // ====================== 微信登录 ======================

    @Operation(summary = "获取微信登录二维码")
    @GetMapping("/wechat/qrcode")
    public Result<WechatQrcodeResponse> getWechatQrcode() {
        return Result.success(authService.getWechatQrcode());
    }

    @Operation(summary = "微信授权登录")
    @PostMapping("/login/wechat")
    public Result<LoginResponse> loginByWechat(@Valid @RequestBody WechatLoginRequest request) {
        return Result.success(authService.loginByWechat(request));
    }

    @Operation(summary = "检查微信扫码登录状态")
    @GetMapping("/wechat/status")
    public Result<LoginResponse> checkWechatLoginStatus(@RequestParam String sceneStr) {
        LoginResponse response = authService.checkWechatLoginStatus(sceneStr);
        if (response == null) {
            return Result.success(null, "等待扫码中");
        }
        return Result.success(response);
    }

    // ====================== 绑定功能 ======================

    @Operation(summary = "绑定手机号")
    @PostMapping("/bind/phone")
    public Result<Void> bindPhone(@CurrentUser UserPrincipal currentUser,
                                   @Valid @RequestBody BindPhoneRequest request) {
        authService.bindPhone(currentUser.getId(), request);
        return Result.success("手机号绑定成功");
    }

    @Operation(summary = "绑定邮箱")
    @PostMapping("/bind/email")
    public Result<Void> bindEmail(@CurrentUser UserPrincipal currentUser,
                                   @Valid @RequestBody BindEmailRequest request) {
        authService.bindEmail(currentUser.getId(), request);
        return Result.success("邮箱绑定成功");
    }

    @Operation(summary = "绑定微信")
    @PostMapping("/bind/wechat")
    public Result<Void> bindWechat(@CurrentUser UserPrincipal currentUser,
                                    @RequestParam String code) {
        authService.bindWechat(currentUser.getId(), code);
        return Result.success("微信绑定成功");
    }

    @Operation(summary = "解绑微信")
    @PostMapping("/unbind/wechat")
    public Result<Void> unbindWechat(@CurrentUser UserPrincipal currentUser) {
        authService.unbindWechat(currentUser.getId());
        return Result.success("微信解绑成功");
    }
}
