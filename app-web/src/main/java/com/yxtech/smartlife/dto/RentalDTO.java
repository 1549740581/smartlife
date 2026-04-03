package com.yxtech.smartlife.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.HouseDetail;
import com.yxtech.smartlife.entity.RentalInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class RentalDTO {

    private Long id;
    private Long publisherUserId;
    private RentalInfo.RentalType rentalType;
    private String title;
    private String description;
    private BigDecimal price;
    private String contactName;
    private String contactPhone;
    private String city;
    private String district;
    private String street;
    private String communityName;
    private LocalDate rentStartDate;
    private LocalDate rentEndDate;
    private List<String> imageUrls;
    private RentalInfo.RentalStatus status;
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private HouseDetailDTO houseDetail;

    public static RentalDTO fromEntity(RentalInfo rentalInfo, ObjectMapper objectMapper) {
        return fromEntity(rentalInfo, null, objectMapper);
    }

    public static RentalDTO fromEntity(RentalInfo rentalInfo, HouseDetail houseDetail, ObjectMapper objectMapper) {
        RentalDTO dto = new RentalDTO();
        dto.setId(rentalInfo.getId());
        dto.setPublisherUserId(rentalInfo.getPublisherUserId());
        dto.setRentalType(rentalInfo.getRentalType());
        dto.setTitle(rentalInfo.getTitle());
        dto.setDescription(rentalInfo.getDescription());
        dto.setPrice(rentalInfo.getPrice());
        dto.setContactName(rentalInfo.getContactName());
        dto.setContactPhone(rentalInfo.getContactPhone());
        dto.setCity(rentalInfo.getCity());
        dto.setDistrict(rentalInfo.getDistrict());
        dto.setStreet(rentalInfo.getStreet());
        dto.setCommunityName(rentalInfo.getCommunityName());
        dto.setRentStartDate(rentalInfo.getRentStartDate());
        dto.setRentEndDate(rentalInfo.getRentEndDate());
        dto.setImageUrls(parseImageUrls(rentalInfo.getImageUrls(), objectMapper));
        dto.setStatus(rentalInfo.getStatus());
        dto.setRejectReason(rentalInfo.getRejectReason());
        dto.setReviewedBy(rentalInfo.getReviewedBy());
        dto.setReviewedAt(rentalInfo.getReviewedAt());
        dto.setCreatedAt(rentalInfo.getCreatedAt());
        dto.setUpdatedAt(rentalInfo.getUpdatedAt());
        dto.setHouseDetail(HouseDetailDTO.fromEntity(houseDetail, objectMapper));
        return dto;
    }

    private static List<String> parseImageUrls(String imageUrls, ObjectMapper objectMapper) {
        if (imageUrls == null || imageUrls.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imageUrls, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
