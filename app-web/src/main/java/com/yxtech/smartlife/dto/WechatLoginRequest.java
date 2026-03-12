package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginRequest {

    @NotBlank
    private String code;

    private String nickname;

    private String avatarUrl;
}
