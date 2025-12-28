package com.interviewkb.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // 认证相关 1xxx
    UNAUTHORIZED(1001, "未登录或token已过期"),
    TOKEN_INVALID(1002, "Token无效"),
    TOKEN_EXPIRED(1003, "Token已过期"),
    ACCESS_DENIED(1004, "权限不足"),
    ACCOUNT_DISABLED(1005, "账号已被禁用"),
    ACCOUNT_LOCKED(1006, "账号已被锁定"),

    // 参数相关 2xxx
    PARAM_ERROR(2001, "参数错误"),
    PARAM_NOT_VALID(2002, "参数校验失败"),
    PARAM_IS_BLANK(2003, "参数为空"),
    PARAM_TYPE_ERROR(2004, "参数类型错误"),

    // 用户相关 3xxx
    USER_NOT_FOUND(3001, "用户不存在"),
    USER_EXISTS(3002, "用户已存在"),
    USERNAME_EXISTS(3003, "用户名已被使用"),
    EMAIL_EXISTS(3004, "邮箱已被注册"),
    PASSWORD_ERROR(3005, "密码错误"),
    OLD_PASSWORD_ERROR(3006, "原密码错误"),
    EMAIL_NOT_VERIFIED(3007, "邮箱未验证"),

    // 业务相关 4xxx
    DATA_NOT_FOUND(4001, "数据不存在"),
    DATA_EXISTS(4002, "数据已存在"),
    OPERATION_FAILED(4003, "操作失败"),
    CATEGORY_HAS_CHILDREN(4004, "分类下存在子分类，无法删除"),
    CATEGORY_HAS_QUESTIONS(4005, "分类下存在题目，无法删除"),

    // 文件相关 5xxx
    FILE_NOT_FOUND(5001, "文件不存在"),
    FILE_UPLOAD_ERROR(5002, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(5003, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(5004, "文件大小超出限制");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
