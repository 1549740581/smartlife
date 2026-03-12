package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.AddressOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressOptionRepository extends JpaRepository<AddressOption, Long> {

    List<AddressOption> findByDeletedFalseOrderByCityAscDistrictAscStreetAscCommunityNameAsc();

    boolean existsByCityAndDistrictAndStreetAndCommunityNameAndDeletedFalse(
            String city,
            String district,
            String street,
            String communityName
    );
}
