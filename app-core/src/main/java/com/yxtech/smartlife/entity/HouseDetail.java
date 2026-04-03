package com.yxtech.smartlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "house_detail")
public class HouseDetail extends BaseEntity {

    @Column(name = "rental_info_id", nullable = false, unique = true)
    private Long rentalInfoId;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "bedroom_count", nullable = false)
    private Integer bedroomCount;

    @Column(name = "living_room_count", nullable = false)
    private Integer livingRoomCount;

    @Column(name = "kitchen_count", nullable = false)
    private Integer kitchenCount;

    @Column(name = "bathroom_count", nullable = false)
    private Integer bathroomCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "orientation", nullable = false, length = 16)
    private Orientation orientation;

    @Column(name = "has_balcony", nullable = false)
    private Boolean hasBalcony;

    @Column(name = "appliances", nullable = false, columnDefinition = "JSON")
    private String appliances;

    @Column(name = "has_elevator", nullable = false)
    private Boolean hasElevator;

    @Column(name = "property_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal propertyFee;

    @Column(name = "water_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal waterFee;

    @Column(name = "electricity_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal electricityFee;

    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;

    @jakarta.persistence.Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public enum Orientation {
        EAST,
        SOUTH,
        WEST,
        NORTH,
        SOUTHEAST,
        SOUTHWEST,
        NORTHEAST,
        NORTHWEST
    }

    public enum Appliance {
        REFRIGERATOR,
        TV,
        AIR_CONDITIONER,
        WASHING_MACHINE,
        WARDROBE,
        NONE
    }
}
