package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRentalRequest {

    @NotNull
    private Boolean approved;

    private String reason;

    @NotBlank
    private String action;
}
