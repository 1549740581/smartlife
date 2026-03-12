package com.yxtech.smartlife.service.impl;

import com.yxtech.smartlife.entity.AddressOption;
import com.yxtech.smartlife.repository.AddressOptionRepository;
import com.yxtech.smartlife.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressOptionRepository addressOptionRepository;

    @Override
    public List<AddressOption> findAllOptions() {
        return addressOptionRepository.findByDeletedFalseOrderByCityAscDistrictAscStreetAscCommunityNameAsc();
    }

    @Override
    public boolean exists(String city, String district, String street, String communityName) {
        return addressOptionRepository.existsByCityAndDistrictAndStreetAndCommunityNameAndDeletedFalse(
                city,
                district,
                street,
                communityName
        );
    }
}
