package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.auth.CurrentAdmin;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.AdminRentalDTO;
import com.yxtech.smartlife.dto.ReviewRentalRequest;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.entity.HouseDetail;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.repository.HouseDetailRepository;
import com.yxtech.smartlife.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rentals")
@RequiredArgsConstructor
public class AdminRentalController {

    private final RentalService rentalService;
    private final HouseDetailRepository houseDetailRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/pending")
    public Result<List<AdminRentalDTO>> getPendingRentals() {
        return Result.success(rentalService.findPendingRentals().stream()
                .map(rental -> AdminRentalDTO.fromEntity(rental, objectMapper))
                .toList());
    }

    @GetMapping
    public Result<List<AdminRentalDTO>> getAllRentals() {
        return Result.success(rentalService.findAllRentals().stream()
                .map(rental -> AdminRentalDTO.fromEntity(rental, objectMapper))
                .toList());
    }

    @GetMapping("/{id}")
    public Result<AdminRentalDTO> getRentalDetail(@PathVariable("id") Long id) {
        RentalInfo rental = rentalService.findRentalById(id);
        HouseDetail houseDetail = null;
        if (rental.getRentalType() == RentalInfo.RentalType.HOUSE) {
            houseDetail = houseDetailRepository.findByRentalInfoIdAndDeletedFalse(rental.getId()).orElse(null);
        }
        return Result.success(AdminRentalDTO.fromEntity(rental, houseDetail, objectMapper));
    }

    @PostMapping("/{id}/review")
    public Result<AdminRentalDTO> reviewRental(
            @CurrentAdmin Admin admin,
            @PathVariable("id") Long id,
            @Valid @RequestBody ReviewRentalRequest request
    ) {
        RentalInfo rentalInfo = rentalService.reviewRental(id, admin.getId(), request.getApproved(), request.getReason());
        return Result.success(AdminRentalDTO.fromEntity(rentalInfo, objectMapper));
    }

    @PostMapping("/{id}/offline")
    public Result<AdminRentalDTO> offlineRental(
            @CurrentAdmin Admin admin,
            @PathVariable("id") Long id,
            @Valid @RequestBody ReviewRentalRequest request
    ) {
        RentalInfo rentalInfo = rentalService.offlineRental(id, admin.getId(), request.getReason());
        return Result.success(AdminRentalDTO.fromEntity(rentalInfo, objectMapper));
    }
}
