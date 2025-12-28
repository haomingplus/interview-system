package com.interviewkb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interviewkb.common.exception.BusinessException;
import com.interviewkb.common.result.ResultCode;
import com.interviewkb.dto.request.LoginRequest;
import com.interviewkb.dto.request.RegisterRequest;
import com.interviewkb.dto.response.LoginResponse;
import com.interviewkb.entity.User;
import com.interviewkb.mapper.UserMapper;
import com.interviewkb.security.CustomUserDetailsService;
import com.interviewkb.security.JwtTokenProvider;
import com.interviewkb.security.UserPrincipal;
import com.interviewkb.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CustomUserDetailsService userDetailsService;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = tokenProvider.generateToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        // 存储refreshToken到Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userPrincipal.getId(),
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        // 更新用户登录信息
        User user = new User();
        user.setId(userPrincipal.getId());
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(1); // 简化处理，实际应该累加
        userMapper.updateById(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpiration())
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(userPrincipal.getId())
                        .username(userPrincipal.getUsername())
                        .email(userPrincipal.getEmail())
                        .nickname(userPrincipal.getNickname())
                        .avatar(userPrincipal.getAvatar())
                        .roles(userPrincipal.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 检查邮箱是否存在
        count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStatus(1);
        user.setEmailVerified(0);

        userMapper.insert(user);

        // 分配默认角色（普通用户）
        // 此处省略角色分配逻辑，实际需要插入sys_user_role表
        log.info("用户注册成功: {}", request.getUsername());
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        // 验证refreshToken是否与Redis中存储的一致
        String storedToken = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserById(userId);

        String newAccessToken = tokenProvider.generateToken(userPrincipal);
        String newRefreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        // 更新Redis中的refreshToken
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                newRefreshToken,
                7,
                TimeUnit.DAYS
        );

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpiration())
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(userPrincipal.getId())
                        .username(userPrincipal.getUsername())
                        .email(userPrincipal.getEmail())
                        .nickname(userPrincipal.getNickname())
                        .avatar(userPrincipal.getAvatar())
                        .roles(userPrincipal.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }

    @Override
    public void logout(String token) {
        if (tokenProvider.validateToken(token)) {
            Long userId = tokenProvider.getUserIdFromToken(token);
            // 将token加入黑名单
            redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + token,
                    "1",
                    24,
                    TimeUnit.HOURS
            );
            // 删除refreshToken
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 实际项目中应该发送邮件
        log.info("发送密码重置邮件到: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // 实际项目中应该验证重置token
        log.info("重置密码");
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(updateUser);
    }
}
