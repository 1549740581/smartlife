package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.RentalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalInfoRepository extends JpaRepository<RentalInfo, Long> {

    Optional<RentalInfo> findByIdAndDeletedFalse(Long id);

    Optional<RentalInfo> findByIdAndPublisherUserIdAndDeletedFalse(Long id, Long publisherUserId);

    List<RentalInfo> findByDeletedFalseOrderByCreatedAtDesc();

    List<RentalInfo> findByStatusAndDeletedFalseOrderByCreatedAtDesc(RentalInfo.RentalStatus status);

    List<RentalInfo> findByRentalTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            RentalInfo.RentalType rentalType,
            RentalInfo.RentalStatus status
    );

    List<RentalInfo> findByPublisherUserIdAndDeletedFalseOrderByCreatedAtDesc(Long publisherUserId);
}
