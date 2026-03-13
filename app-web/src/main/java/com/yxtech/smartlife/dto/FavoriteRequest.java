package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long rentalInfoId;
}
