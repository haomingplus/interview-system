package com.interviewkb.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginRequest {

    @NotBlank(message = "微信授权码不能为空")
    private String code;

    /**
     * 登录场景: pc-PC端扫码 h5-H5端授权 mini-小程序
     */
    private String scene = "pc";
}
