package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.CreateRentalRequest;
import com.yxtech.smartlife.dto.HouseDetailRequest;
import com.yxtech.smartlife.dto.OfflineRentalRequest;
import com.yxtech.smartlife.dto.RentalDTO;
import com.yxtech.smartlife.dto.UpdateRentalRequest;
import com.yxtech.smartlife.entity.HouseDetail;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.repository.HouseDetailRepository;
import com.yxtech.smartlife.service.RentalService;
import com.yxtech.smartlife.service.command.CreateHouseDetailCommand;
import com.yxtech.smartlife.service.command.CreateRentalCommand;
import com.yxtech.smartlife.service.command.UpdateRentalCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final HouseDetailRepository houseDetailRepository;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Result<RentalDTO>> createRental(@Valid @RequestBody CreateRentalRequest request) {
        CreateRentalCommand command = CreateRentalCommand.builder()
                .publisherUserId(request.getPublisherUserId())
                .rentalType(request.getRentalType())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .city(request.getCity())
                .district(request.getDistrict())
                .street(request.getStreet())
                .communityName(request.getCommunityName())
                .imageUrls(request.getImageUrls())
                .houseDetail(toHouseDetailCommand(request.getHouseDetail()))
                .isDraft(request.getIsDraft())
                .build();
        RentalInfo saved = rentalService.createRental(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(RentalDTO.fromEntity(saved, objectMapper)));
    }

    @PutMapping("/{id}")
    public Result<RentalDTO> updateRental(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateRentalRequest request
    ) {
        UpdateRentalCommand command = UpdateRentalCommand.builder()
                .rentalId(id)
                .userId(request.getUserId())
                .rentalType(request.getRentalType())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .city(request.getCity())
                .district(request.getDistrict())
                .street(request.getStreet())
                .communityName(request.getCommunityName())
                .imageUrls(request.getImageUrls())
                .houseDetail(toHouseDetailCommand(request.getHouseDetail()))
                .isDraft(request.getIsDraft())
                .build();
        RentalInfo saved = rentalService.updateRental(command);
        HouseDetail houseDetail = null;
        if (saved.getRentalType() == RentalInfo.RentalType.HOUSE) {
            houseDetail = houseDetailRepository.findByRentalInfoIdAndDeletedFalse(saved.getId()).orElse(null);
        }
        return Result.success(RentalDTO.fromEntity(saved, houseDetail, objectMapper));
    }

    private CreateHouseDetailCommand toHouseDetailCommand(HouseDetailRequest req) {
        if (req == null) {
            return null;
        }
        return CreateHouseDetailCommand.builder()
                .floor(req.getFloor())
                .bedroomCount(req.getBedroomCount())
                .livingRoomCount(req.getLivingRoomCount())
                .kitchenCount(req.getKitchenCount())
                .bathroomCount(req.getBathroomCount())
                .orientation(req.getOrientation())
                .hasBalcony(req.getHasBalcony())
                .appliances(req.getAppliances())
                .hasElevator(req.getHasElevator())
                .propertyFee(req.getPropertyFee())
                .waterFee(req.getWaterFee())
                .electricityFee(req.getElectricityFee())
                .extraInfo(req.getExtraInfo())
                .build();
    }

    @GetMapping
    public Result<List<RentalDTO>> getPublicRentals(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) RentalInfo.RentalType type,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "street", required = false) String street,
            @RequestParam(value = "communityName", required = false) String communityName
    ) {
        return Result.success(rentalService.searchPublicRentals(keyword, type, city, district, street, communityName).stream()
                .map(rental -> RentalDTO.fromEntity(rental, objectMapper))
                .toList());
    }

    @GetMapping("/{id}")
    public Result<RentalDTO> getPublicRentalDetail(@PathVariable("id") Long id) {
        RentalInfo rental = rentalService.findPublicRentalById(id);
        HouseDetail houseDetail = null;
        if (rental.getRentalType() == RentalInfo.RentalType.HOUSE) {
            houseDetail = houseDetailRepository.findByRentalInfoIdAndDeletedFalse(rental.getId()).orElse(null);
        }
        return Result.success(RentalDTO.fromEntity(rental, houseDetail, objectMapper));
    }

    @GetMapping("/type/{type}")
    public Result<List<RentalDTO>> getPublicRentalsByType(
            @PathVariable("type") RentalInfo.RentalType type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "street", required = false) String street,
            @RequestParam(value = "communityName", required = false) String communityName
    ) {
        return Result.success(rentalService.searchPublicRentals(keyword, type, city, district, street, communityName).stream()
                .map(rental -> RentalDTO.fromEntity(rental, objectMapper))
                .toList());
    }

    @GetMapping("/user/{userId}")
    public Result<List<RentalDTO>> getUserRentals(@PathVariable("userId") Long userId) {
        return Result.success(rentalService.findUserRentals(userId).stream()
                .map(rental -> RentalDTO.fromEntity(rental, objectMapper))
                .toList());
    }

    @GetMapping("/user/{userId}/{id}")
    public Result<RentalDTO> getUserRentalDetail(
            @PathVariable("userId") Long userId,
            @PathVariable("id") Long id
    ) {
        RentalInfo rental = rentalService.findUserRentalById(userId, id);
        HouseDetail houseDetail = null;
        if (rental.getRentalType() == RentalInfo.RentalType.HOUSE) {
            houseDetail = houseDetailRepository.findByRentalInfoIdAndDeletedFalse(rental.getId()).orElse(null);
        }
        return Result.success(RentalDTO.fromEntity(rental, houseDetail, objectMapper));
    }

    @PostMapping("/{id}/offline")
    public Result<RentalDTO> userOfflineRental(
            @PathVariable("id") Long id,
            @Valid @RequestBody OfflineRentalRequest request
    ) {
        RentalInfo saved = rentalService.userOfflineRental(id, request.getUserId());
        return Result.success(RentalDTO.fromEntity(saved, objectMapper));
    }

    @PostMapping("/{id}/online")
    public Result<RentalDTO> userOnlineRental(
            @PathVariable("id") Long id,
            @Valid @RequestBody OfflineRentalRequest request
    ) {
        RentalInfo saved = rentalService.userOnlineRental(id, request.getUserId());
        return Result.success(RentalDTO.fromEntity(saved, objectMapper));
    }
}
