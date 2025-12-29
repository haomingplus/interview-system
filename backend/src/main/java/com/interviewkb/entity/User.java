package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer phoneVerified;
    private String wechatOpenid;
    private String wechatUnionid;
    private String wechatNickname;
    private String wechatAvatar;
    private Integer gender;
    private String bio;
    private Integer status;
    private Integer emailVerified;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer loginCount;

    @TableField(exist = false)
    private List<Role> roles;
}
