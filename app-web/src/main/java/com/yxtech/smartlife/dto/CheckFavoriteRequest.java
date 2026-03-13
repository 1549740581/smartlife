package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckFavoriteRequest {

    @NotNull
    private Long userId;

    private List<Long> rentalInfoIds;
}
