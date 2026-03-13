package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCancelOrderRequest {

    @NotBlank
    private String reason;
}
