package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.CreateRentalRequest;
import com.yxtech.smartlife.dto.RentalDTO;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.service.RentalService;
import com.yxtech.smartlife.service.command.CreateRentalCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
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
                .communityName(request.getCommunityName())
                .imageUrls(request.getImageUrls())
                .build();
        RentalInfo saved = rentalService.createRental(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(RentalDTO.fromEntity(saved, objectMapper)));
    }

    @GetMapping
    public Result<List<RentalDTO>> getPublicRentals() {
        return Result.success(rentalService.findPublicRentals().stream()
                .map(rental -> RentalDTO.fromEntity(rental, objectMapper))
                .toList());
    }

    @GetMapping("/{id}")
    public Result<RentalDTO> getPublicRentalDetail(@PathVariable("id") Long id) {
        return Result.success(RentalDTO.fromEntity(rentalService.findPublicRentalById(id), objectMapper));
    }

    @GetMapping("/type/{type}")
    public Result<List<RentalDTO>> getPublicRentalsByType(@PathVariable("type") RentalInfo.RentalType type) {
        return Result.success(rentalService.findPublicRentalsByType(type).stream()
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
        return Result.success(RentalDTO.fromEntity(rentalService.findUserRentalById(userId, id), objectMapper));
    }
}
