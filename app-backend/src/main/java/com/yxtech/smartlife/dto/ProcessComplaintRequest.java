package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessComplaintRequest {

    @NotNull
    private Long id;

    @NotNull
    private Boolean accepted;

    private String remark;
}
