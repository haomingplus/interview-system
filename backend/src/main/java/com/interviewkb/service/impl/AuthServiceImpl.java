package com.interviewkb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interviewkb.common.exception.BusinessException;
import com.interviewkb.common.result.ResultCode;
import com.interviewkb.dto.request.*;
import com.interviewkb.dto.response.LoginResponse;
import com.interviewkb.dto.response.WechatQrcodeResponse;
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
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
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
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String WECHAT_SCENE_PREFIX = "wechat:scene:";
    private static final String WECHAT_LOGIN_PREFIX = "wechat:login:";

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateLoginResponse(userPrincipal);
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
        log.info("用户注册成功: {}", request.getUsername());
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        String storedToken = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserById(userId);
        return generateLoginResponse(userPrincipal);
    }

    @Override
    public void logout(String token) {
        if (tokenProvider.validateToken(token)) {
            Long userId = tokenProvider.getUserIdFromToken(token);
            redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "1", 24, TimeUnit.HOURS);
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        log.info("发送密码重置邮件到: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("重置密码");
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (user.getPassword() != null && !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(updateUser);
    }

    // ====================== 手机验证码登录 ======================

    @Override
    public void sendSmsCode(SmsCodeRequest request) {
        String phone = request.getPhone();
        String purpose = request.getPurpose();

        // 检查发送频率限制（1分钟内不能重复发送）
        String rateLimitKey = SMS_CODE_PREFIX + "limit:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new BusinessException("发送过于频繁，请1分钟后重试");
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 存储验证码到Redis，5分钟有效
        String codeKey = SMS_CODE_PREFIX + purpose + ":" + phone;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);

        // 设置发送频率限制
        redisTemplate.opsForValue().set(rateLimitKey, "1", 1, TimeUnit.MINUTES);

        // TODO: 实际项目中调用短信服务商API发送短信
        log.info("发送验证码到手机 {}: {}", phone, code);
    }

    @Override
    @Transactional
    public LoginResponse loginByPhone(PhoneLoginRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();

        // 验证验证码
        String codeKey = SMS_CODE_PREFIX + "login:" + phone;
        String storedCode = (String) redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 删除已使用的验证码
        redisTemplate.delete(codeKey);

        // 查询或创建用户
        User user = userMapper.selectByPhoneWithRoles(phone);
        if (user == null) {
            // 自动注册新用户
            user = createUserByPhone(phone);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新登录信息
        updateLoginInfo(user.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        return generateLoginResponse(userPrincipal);
    }

    private User createUserByPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setPhoneVerified(1);
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setStatus(1);
        userMapper.insert(user);

        // 查询带角色的用户信息
        return userMapper.selectByPhoneWithRoles(phone);
    }

    // ====================== 微信登录 ======================

    @Override
    public WechatQrcodeResponse getWechatQrcode() {
        // 生成场景值
        String sceneStr = UUID.randomUUID().toString().replace("-", "");

        // 存储场景值，5分钟有效
        redisTemplate.opsForValue().set(WECHAT_SCENE_PREFIX + sceneStr, "pending", 5, TimeUnit.MINUTES);

        // TODO: 实际项目中调用微信API获取二维码
        // 这里模拟返回
        return WechatQrcodeResponse.builder()
                .sceneStr(sceneStr)
                .qrcodeUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + sceneStr)
                .expireSeconds(300)
                .build();
    }

    @Override
    @Transactional
    public LoginResponse loginByWechat(WechatLoginRequest request) {
        String code = request.getCode();

        // TODO: 实际项目中调用微信API获取openid和用户信息
        // 这里模拟微信返回的数据
        String openid = "wx_" + code;
        String nickname = "微信用户";
        String avatar = "";

        // 查询或创建用户
        User user = userMapper.selectByWechatOpenidWithRoles(openid);
        if (user == null) {
            user = createUserByWechat(openid, nickname, avatar);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        updateLoginInfo(user.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        return generateLoginResponse(userPrincipal);
    }

    @Override
    public LoginResponse checkWechatLoginStatus(String sceneStr) {
        // 检查是否有用户扫码登录
        String loginKey = WECHAT_LOGIN_PREFIX + sceneStr;
        Object userId = redisTemplate.opsForValue().get(loginKey);

        if (userId == null) {
            // 检查场景值是否还有效
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(WECHAT_SCENE_PREFIX + sceneStr))) {
                throw new BusinessException("二维码已过期，请刷新");
            }
            return null; // 还在等待扫码
        }

        // 用户已扫码，生成登录响应
        redisTemplate.delete(loginKey);
        redisTemplate.delete(WECHAT_SCENE_PREFIX + sceneStr);

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserById(Long.valueOf(userId.toString()));
        return generateLoginResponse(userPrincipal);
    }

    private User createUserByWechat(String openid, String nickname, String avatar) {
        User user = new User();
        user.setWechatOpenid(openid);
        user.setWechatNickname(nickname);
        user.setWechatAvatar(avatar);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setStatus(1);
        userMapper.insert(user);

        return userMapper.selectByWechatOpenidWithRoles(openid);
    }

    // ====================== 绑定功能 ======================

    @Override
    @Transactional
    public void bindPhone(Long userId, BindPhoneRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();

        // 验证验证码
        String codeKey = SMS_CODE_PREFIX + "bind:" + phone;
        String storedCode = (String) redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new BusinessException("验证码错误或已过期");
        }
        redisTemplate.delete(codeKey);

        // 检查手机号是否已被绑定
        User existingUser = userMapper.selectByPhone(phone);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException("该手机号已被其他账号绑定");
        }

        // 更新用户手机号
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPhone(phone);
        updateUser.setPhoneVerified(1);
        userMapper.updateById(updateUser);

        log.info("用户 {} 绑定手机号: {}", userId, phone);
    }

    @Override
    @Transactional
    public void bindEmail(Long userId, BindEmailRequest request) {
        String email = request.getEmail();

        // 检查邮箱是否已被绑定
        User existingUser = userMapper.selectByEmail(email);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException("该邮箱已被其他账号绑定");
        }

        // TODO: 验证邮箱验证码

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setEmail(email);
        updateUser.setEmailVerified(1);
        userMapper.updateById(updateUser);

        log.info("用户 {} 绑定邮箱: {}", userId, email);
    }

    @Override
    @Transactional
    public void bindWechat(Long userId, String code) {
        // TODO: 调用微信API获取openid
        String openid = "wx_bind_" + code;

        // 检查微信是否已被绑定
        User existingUser = userMapper.selectByWechatOpenid(openid);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException("该微信已被其他账号绑定");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setWechatOpenid(openid);
        userMapper.updateById(updateUser);

        log.info("用户 {} 绑定微信", userId);
    }

    @Override
    @Transactional
    public void unbindWechat(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 确保用户还有其他登录方式
        if (user.getPhone() == null && user.getEmail() == null) {
            throw new BusinessException("请先绑定手机号或邮箱后再解绑微信");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setWechatOpenid(null);
        updateUser.setWechatUnionid(null);
        updateUser.setWechatNickname(null);
        updateUser.setWechatAvatar(null);
        userMapper.updateById(updateUser);

        log.info("用户 {} 解绑微信", userId);
    }

    // ====================== 辅助方法 ======================

    private LoginResponse generateLoginResponse(UserPrincipal userPrincipal) {
        String accessToken = tokenProvider.generateToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        // 存储refreshToken到Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userPrincipal.getId(),
                refreshToken,
                7,
                TimeUnit.DAYS
        );

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
                        .roles(userPrincipal.getAuthorities() != null ?
                                userPrincipal.getAuthorities().stream()
                                        .map(GrantedAuthority::getAuthority)
                                        .collect(Collectors.toList()) :
                                Collections.emptyList())
                        .build())
                .build();
    }

    private void updateLoginInfo(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(1);
        userMapper.updateById(user);
    }
}
