package com.yxtech.smartlife.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.HouseDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Data
public class AdminHouseDetailDTO {

    private Long id;
    private Integer floor;
    private Integer bedroomCount;
    private Integer livingRoomCount;
    private Integer kitchenCount;
    private Integer bathroomCount;
    private String orientation;
    private String orientationLabel;
    private Boolean hasBalcony;
    private List<String> appliances;
    private List<String> applianceLabels;
    private Boolean hasElevator;
    private BigDecimal propertyFee;
    private BigDecimal waterFee;
    private BigDecimal electricityFee;
    private String extraInfo;

    public static AdminHouseDetailDTO fromEntity(HouseDetail entity, ObjectMapper objectMapper) {
        if (entity == null) {
            return null;
        }
        AdminHouseDetailDTO dto = new AdminHouseDetailDTO();
        dto.setId(entity.getId());
        dto.setFloor(entity.getFloor());
        dto.setBedroomCount(entity.getBedroomCount());
        dto.setLivingRoomCount(entity.getLivingRoomCount());
        dto.setKitchenCount(entity.getKitchenCount());
        dto.setBathroomCount(entity.getBathroomCount());
        dto.setOrientation(entity.getOrientation() != null ? entity.getOrientation().name() : null);
        dto.setOrientationLabel(getOrientationLabel(entity.getOrientation()));
        dto.setHasBalcony(entity.getHasBalcony());
        dto.setAppliances(parseAppliances(entity.getAppliances(), objectMapper));
        dto.setApplianceLabels(getApplianceLabels(dto.getAppliances()));
        dto.setHasElevator(entity.getHasElevator());
        dto.setPropertyFee(entity.getPropertyFee());
        dto.setWaterFee(entity.getWaterFee());
        dto.setElectricityFee(entity.getElectricityFee());
        dto.setExtraInfo(entity.getExtraInfo());
        return dto;
    }

    private static String getOrientationLabel(HouseDetail.Orientation orientation) {
        if (orientation == null) {
            return null;
        }
        return switch (orientation) {
            case EAST -> "东";
            case SOUTH -> "南";
            case WEST -> "西";
            case NORTH -> "北";
            case SOUTHEAST -> "东南";
            case SOUTHWEST -> "西南";
            case NORTHEAST -> "东北";
            case NORTHWEST -> "西北";
        };
    }

    private static List<String> parseAppliances(String appliances, ObjectMapper objectMapper) {
        if (appliances == null || appliances.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(appliances, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private static List<String> getApplianceLabels(List<String> appliances) {
        if (appliances == null || appliances.isEmpty()) {
            return Collections.emptyList();
        }
        return appliances.stream().map(AdminHouseDetailDTO::getApplianceLabel).toList();
    }

    private static String getApplianceLabel(String appliance) {
        return switch (appliance) {
            case "REFRIGERATOR" -> "冰箱";
            case "TV" -> "电视";
            case "AIR_CONDITIONER" -> "空调";
            case "WASHING_MACHINE" -> "洗衣机";
            case "WARDROBE" -> "衣柜";
            case "NONE" -> "无";
            default -> appliance;
        };
    }
}
