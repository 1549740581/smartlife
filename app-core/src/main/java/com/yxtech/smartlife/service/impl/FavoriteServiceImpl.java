package com.yxtech.smartlife.service.impl;

import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.UserFavorite;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.UserFavoriteRepository;
import com.yxtech.smartlife.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final RentalInfoRepository rentalInfoRepository;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long rentalInfoId) {
        // 检查是否为自己发布的信息
        RentalInfo rental = rentalInfoRepository.findByIdAndDeletedFalse(rentalInfoId)
                .orElseThrow(() -> new IllegalArgumentException("rental not found"));
        if (rental.getPublisherUserId().equals(userId)) {
            throw new IllegalArgumentException("cannot favorite your own rental");
        }
        if (favoriteRepository.existsByUserIdAndRentalInfoId(userId, rentalInfoId)) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setRentalInfoId(rentalInfoId);
        favoriteRepository.save(favorite);
        log.info("User {} added favorite for rental {}", userId, rentalInfoId);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long rentalInfoId) {
        favoriteRepository.deleteByUserIdAndRentalInfoId(userId, rentalInfoId);
        log.info("User {} removed favorite for rental {}", userId, rentalInfoId);
    }

    @Override
    public boolean isFavorite(Long userId, Long rentalInfoId) {
        return favoriteRepository.existsByUserIdAndRentalInfoId(userId, rentalInfoId);
    }

    @Override
    public List<RentalInfo> getUserFavorites(Long userId) {
        List<UserFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (favorites.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> rentalIds = favorites.stream().map(UserFavorite::getRentalInfoId).toList();
        return rentalInfoRepository.findAllById(rentalIds);
    }

    @Override
    public Set<Long> filterFavoriteIds(Long userId, List<Long> rentalInfoIds) {
        if (userId == null || rentalInfoIds == null || rentalInfoIds.isEmpty()) {
            return Collections.emptySet();
        }
        return favoriteRepository.findFavoriteRentalIds(userId, rentalInfoIds);
    }
}
