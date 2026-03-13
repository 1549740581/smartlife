package com.yxtech.smartlife.dto.adminweb;

import lombok.Data;

@Data
public class AdminPageRequest {
    private Integer page = 1;
    private Integer pageSize = 20;
    private String keyword;
    private String status;
    private String rentalType;
}
