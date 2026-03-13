package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.CheckFavoriteRequest;
import com.yxtech.smartlife.dto.FavoriteListRequest;
import com.yxtech.smartlife.dto.FavoriteRequest;
import com.yxtech.smartlife.dto.RentalDTO;
import com.yxtech.smartlife.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ObjectMapper objectMapper;

    @PostMapping("/add")
    public Result<Void> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        favoriteService.addFavorite(request.getUserId(), request.getRentalInfoId());
        return Result.success();
    }

    @PostMapping("/remove")
    public Result<Void> removeFavorite(@Valid @RequestBody FavoriteRequest request) {
        favoriteService.removeFavorite(request.getUserId(), request.getRentalInfoId());
        return Result.success();
    }

    @PostMapping("/check")
    public Result<Boolean> checkFavorite(@Valid @RequestBody FavoriteRequest request) {
        return Result.success(favoriteService.isFavorite(request.getUserId(), request.getRentalInfoId()));
    }

    @PostMapping("/list")
    public Result<List<RentalDTO>> getFavorites(@Valid @RequestBody FavoriteListRequest request) {
        return Result.success(favoriteService.getUserFavorites(request.getUserId()).stream()
                .map(info -> RentalDTO.fromEntity(info, objectMapper))
                .toList());
    }

    @PostMapping("/filter-ids")
    public Result<Set<Long>> filterFavoriteIds(@Valid @RequestBody CheckFavoriteRequest request) {
        return Result.success(favoriteService.filterFavoriteIds(request.getUserId(), request.getRentalInfoIds()));
    }
}
