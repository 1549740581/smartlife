package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConversationListRequest {

    @NotNull
    private Long userId;
}
