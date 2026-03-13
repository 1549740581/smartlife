package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MarkReadRequest {

    @NotNull
    private Long conversationId;

    @NotNull
    private Long userId;
}
