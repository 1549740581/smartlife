package com.yxtech.smartlife.service.model;

import com.yxtech.smartlife.entity.RentalConversation;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.RentalMessage;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.entity.User;

public record RentalConversationSummaryAggregate(
        RentalConversation conversation,
        RentalInfo rentalInfo,
        User landlord,
        User tenant,
        RentalMessage lastMessage,
        RentalOrder latestOrder
) {
}
