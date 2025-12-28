package com.interviewkb.controller;

import com.interviewkb.common.result.Result;
import com.interviewkb.dto.request.LoginRequest;
import com.interviewkb.dto.request.RegisterRequest;
import com.interviewkb.dto.response.LoginResponse;
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

    @Operation(summary = "用户登录")
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
}
