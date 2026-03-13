package com.yxtech.smartlife.dto.adminweb;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long id;
    private Boolean approved;
    private String reason;
}
