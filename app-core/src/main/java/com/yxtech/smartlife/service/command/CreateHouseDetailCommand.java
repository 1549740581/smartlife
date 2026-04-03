package com.yxtech.smartlife.service.command;

import com.yxtech.smartlife.entity.HouseDetail;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class CreateHouseDetailCommand {
    Integer floor;
    Integer bedroomCount;
    Integer livingRoomCount;
    Integer kitchenCount;
    Integer bathroomCount;
    HouseDetail.Orientation orientation;
    Boolean hasBalcony;
    List<HouseDetail.Appliance> appliances;
    Boolean hasElevator;
    BigDecimal propertyFee;
    BigDecimal waterFee;
    BigDecimal electricityFee;
    String extraInfo;
}
