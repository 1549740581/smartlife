package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.service.command.CreateRentalCommand;

import java.util.List;

public interface RentalService {

    RentalInfo createRental(CreateRentalCommand command);

    List<RentalInfo> findPublicRentals();

    List<RentalInfo> searchPublicRentals(
            String keyword,
            RentalInfo.RentalType rentalType,
            String city,
            String district,
            String street,
            String communityName
    );

    RentalInfo findPublicRentalById(Long rentalId);

    List<RentalInfo> findPublicRentalsByType(RentalInfo.RentalType rentalType);

    List<RentalInfo> findUserRentals(Long userId);

    RentalInfo findUserRentalById(Long userId, Long rentalId);

    List<RentalInfo> findAllRentals();

    RentalInfo findRentalById(Long rentalId);

    List<RentalInfo> findPendingRentals();

    RentalInfo reviewRental(Long rentalId, Long adminId, boolean approved, String reason);

    RentalInfo offlineRental(Long rentalId, Long adminId, String reason);
}
