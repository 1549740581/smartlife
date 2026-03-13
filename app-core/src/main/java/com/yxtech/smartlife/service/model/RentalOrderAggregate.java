package com.yxtech.smartlife.service.model;

import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.entity.User;

public record RentalOrderAggregate(
        RentalOrder order,
        RentalInfo rentalInfo,
        User landlord,
        User tenant
) {
}
