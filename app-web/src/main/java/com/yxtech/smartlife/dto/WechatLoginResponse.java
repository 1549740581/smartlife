package com.yxtech.smartlife.dto;

import lombok.Data;

@Data
public class WechatLoginResponse {

    private Long userId;
    private String openId;
    private String nickname;
}
