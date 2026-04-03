package com.yxtech.smartlife.service.command;

import com.yxtech.smartlife.entity.RentalInfo;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class UpdateRentalCommand {
    Long rentalId;
    Long userId;
    RentalInfo.RentalType rentalType;
    String title;
    String description;
    BigDecimal price;
    String contactName;
    String contactPhone;
    String city;
    String district;
    String street;
    String communityName;
    List<String> imageUrls;
    CreateHouseDetailCommand houseDetail;
    Boolean isDraft;
}
