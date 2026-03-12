package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.AddressOption;

import java.util.List;

public interface AddressService {

    List<AddressOption> findAllOptions();

    boolean exists(String city, String district, String street, String communityName);
}
