package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateRentalOrderRequest {

    @NotNull
    private Long userId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
