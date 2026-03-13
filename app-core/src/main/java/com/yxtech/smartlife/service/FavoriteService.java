package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.RentalInfo;

import java.util.List;
import java.util.Set;

public interface FavoriteService {

    void addFavorite(Long userId, Long rentalInfoId);

    void removeFavorite(Long userId, Long rentalInfoId);

    boolean isFavorite(Long userId, Long rentalInfoId);

    List<RentalInfo> getUserFavorites(Long userId);

    Set<Long> filterFavoriteIds(Long userId, List<Long> rentalInfoIds);
}
