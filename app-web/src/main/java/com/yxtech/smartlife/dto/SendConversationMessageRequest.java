package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendConversationMessageRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String content;
}
