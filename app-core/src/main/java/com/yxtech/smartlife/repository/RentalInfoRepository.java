package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.RentalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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

    @Query("""
            select r from RentalInfo r
            where r.deleted = false
              and r.status = :status
              and (:rentalType is null or r.rentalType = :rentalType)
              and (:city is null or r.city = :city)
              and (:district is null or r.district = :district)
              and (:street is null or r.street = :street)
              and (:communityName is null or r.communityName = :communityName)
              and (
                    :keyword is null
                    or lower(r.title) like lower(concat('%', :keyword, '%'))
                    or lower(r.description) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(r.city, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(r.district, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(r.street, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(r.communityName, '')) like lower(concat('%', :keyword, '%'))
                  )
            order by r.createdAt desc
            """)
    List<RentalInfo> searchPublicRentals(
            @Param("status") RentalInfo.RentalStatus status,
            @Param("keyword") String keyword,
            @Param("rentalType") RentalInfo.RentalType rentalType,
            @Param("city") String city,
            @Param("district") String district,
            @Param("street") String street,
            @Param("communityName") String communityName
    );

    List<RentalInfo> findByPublisherUserIdAndDeletedFalseOrderByCreatedAtDesc(Long publisherUserId);

    Page<RentalInfo> findByDeletedFalse(Pageable pageable);

    Page<RentalInfo> findByStatusAndDeletedFalse(RentalInfo.RentalStatus status, Pageable pageable);

    long countByStatusAndDeletedFalse(RentalInfo.RentalStatus status);

    long countByRentalTypeAndDeletedFalse(RentalInfo.RentalType rentalType);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<RentalInfo> findByPublisherUserIdAndDeletedFalse(Long publisherUserId);

    long countByPublisherUserIdAndDeletedFalse(Long publisherUserId);
}
