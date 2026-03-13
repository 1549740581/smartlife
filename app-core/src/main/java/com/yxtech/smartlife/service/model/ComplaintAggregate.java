package com.yxtech.smartlife.service.model;

import com.yxtech.smartlife.entity.Complaint;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.User;

public record ComplaintAggregate(
        Complaint complaint,
        User complainant,
        User targetUser,
        RentalInfo rentalInfo
) {
}
