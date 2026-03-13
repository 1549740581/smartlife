package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteListRequest {

    @NotNull
    private Long userId;
}
