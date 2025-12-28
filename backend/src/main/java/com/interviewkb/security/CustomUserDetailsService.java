package com.interviewkb.security;

import com.interviewkb.entity.User;
import com.interviewkb.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        User user;

        // 尝试按ID加载
        try {
            Long userId = Long.parseLong(usernameOrId);
            user = userMapper.selectByIdWithRoles(userId);
        } catch (NumberFormatException e) {
            // 按用户名加载
            user = userMapper.selectByUsernameWithRoles(usernameOrId);
        }

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + usernameOrId);
        }

        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已被禁用");
        }

        return UserPrincipal.create(user);
    }

    public UserDetails loadUserById(Long userId) {
        User user = userMapper.selectByIdWithRoles(userId);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }
        return UserPrincipal.create(user);
    }
}
