package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interviewkb.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByUsernameWithRoles(@Param("username") String username);

    User selectByIdWithRoles(@Param("id") Long id);

    User selectByEmail(@Param("email") String email);

    User selectByPhone(@Param("phone") String phone);

    User selectByPhoneWithRoles(@Param("phone") String phone);

    User selectByWechatOpenid(@Param("openid") String openid);

    User selectByWechatOpenidWithRoles(@Param("openid") String openid);
}
