package com.yxtech.smartlife.dto.adminweb;

import lombok.Data;

@Data
public class AddressRequest {
    private Long id;
    private String city;
    private String district;
    private String street;
    private String communityName;
}
