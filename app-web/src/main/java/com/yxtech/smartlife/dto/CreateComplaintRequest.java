package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateComplaintRequest {

    @NotNull
    private Long complainantUserId;

    @NotNull
    private Long rentalInfoId;

    @NotBlank
    private String reason;

    private List<String> evidenceUrls;
}
