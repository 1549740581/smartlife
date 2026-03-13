package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    Optional<UserFavorite> findByUserIdAndRentalInfoId(Long userId, Long rentalInfoId);

    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndRentalInfoId(Long userId, Long rentalInfoId);

    void deleteByUserIdAndRentalInfoId(Long userId, Long rentalInfoId);

    @Query("SELECT f.rentalInfoId FROM UserFavorite f WHERE f.userId = :userId AND f.rentalInfoId IN :rentalInfoIds")
    Set<Long> findFavoriteRentalIds(@Param("userId") Long userId, @Param("rentalInfoIds") List<Long> rentalInfoIds);
}
