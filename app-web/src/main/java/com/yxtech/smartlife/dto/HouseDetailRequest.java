package com.yxtech.smartlife.dto;

import com.yxtech.smartlife.entity.HouseDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseDetailRequest {

    private Integer floor;

    private Integer bedroomCount;

    private Integer livingRoomCount;

    private Integer kitchenCount;

    private Integer bathroomCount;

    private HouseDetail.Orientation orientation;

    private Boolean hasBalcony;

    private List<HouseDetail.Appliance> appliances;

    private Boolean hasElevator;

    private BigDecimal propertyFee;

    private BigDecimal waterFee;

    private BigDecimal electricityFee;

    private String extraInfo;
}
