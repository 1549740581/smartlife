package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.HouseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseDetailRepository extends JpaRepository<HouseDetail, Long> {

    Optional<HouseDetail> findByRentalInfoIdAndDeletedFalse(Long rentalInfoId);

    Optional<HouseDetail> findByRentalInfoId(Long rentalInfoId);
}
