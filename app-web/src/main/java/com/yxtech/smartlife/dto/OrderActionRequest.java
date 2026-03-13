package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderActionRequest {

    @NotNull
    private Long userId;

    private String reason;
}
