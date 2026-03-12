package com.yxtech.smartlife.dto;

import com.yxtech.smartlife.entity.RentalInfo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateRentalRequest {

    @NotNull
    private Long publisherUserId;

    @NotNull
    private RentalInfo.RentalType rentalType;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotBlank
    private String contactName;

    @NotBlank
    private String contactPhone;

    @NotBlank
    private String city;

    private String district;

    private String street;

    @NotBlank
    private String communityName;

    private List<String> imageUrls;
}
