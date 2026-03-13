package com.yxtech.smartlife.dto.adminweb;

import lombok.Data;

@Data
public class ProcessComplaintRequest {
    private Long id;
    private Boolean accepted;
    private String result;
}
