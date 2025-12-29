package com.interviewkb.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WechatQrcodeResponse {

    /**
     * 二维码票据，用于换取二维码图片
     */
    private String ticket;

    /**
     * 二维码图片URL
     */
    private String qrcodeUrl;

    /**
     * 场景值，用于轮询登录状态
     */
    private String sceneStr;

    /**
     * 二维码有效期（秒）
     */
    private Integer expireSeconds;
}
