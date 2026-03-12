package com.yxtech.smartlife.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminLoginResponse {

    private Long adminId;
    private String displayName;
    private String adminToken;
    private LocalDateTime expiresAt;
}
