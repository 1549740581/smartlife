package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.RentalOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {

    Optional<RentalOrder> findByIdAndDeletedFalse(Long id);

    List<RentalOrder> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(Long conversationId);

    List<RentalOrder> findByTenantUserIdAndDeletedFalseOrderByCreatedAtDesc(Long tenantUserId);

    List<RentalOrder> findByLandlordUserIdAndDeletedFalseOrderByCreatedAtDesc(Long landlordUserId);

    List<RentalOrder> findByDeletedFalseOrderByCreatedAtDesc();

    @Query("""
            select o from RentalOrder o
            where o.deleted = false
              and o.rentalInfoId = :rentalInfoId
              and o.status in :statuses
              and o.startDate <= :endDate
              and o.endDate >= :startDate
            order by o.createdAt desc
            """)
    List<RentalOrder> findOverlappingOrders(
            @Param("rentalInfoId") Long rentalInfoId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<RentalOrder.OrderStatus> statuses
    );

    @Query("""
            select o from RentalOrder o
            where o.deleted = false
              and o.rentalInfoId = :rentalInfoId
              and o.status in :statuses
            order by o.startDate desc, o.createdAt desc
            """)
    List<RentalOrder> findCurrentOrders(
            @Param("rentalInfoId") Long rentalInfoId,
            @Param("statuses") Collection<RentalOrder.OrderStatus> statuses
    );

    @Query("""
            select o from RentalOrder o
            where o.deleted = false
              and o.status = com.yxtech.smartlife.entity.RentalOrder$OrderStatus.ACTIVE
              and o.endDate < :today
            """)
    List<RentalOrder> findExpiredActiveOrders(@Param("today") LocalDate today);

    @Query("""
            select o from RentalOrder o
            where o.deleted = false
              and o.status = com.yxtech.smartlife.entity.RentalOrder$OrderStatus.ACTIVE
              and o.endDate = :reminderDate
              and o.reminderSentAt is null
            """)
    List<RentalOrder> findOrdersNeedReminder(@Param("reminderDate") LocalDate reminderDate);

    @Query("""
            select o from RentalOrder o
            where o.deleted = false
              and o.rentalInfoId = :rentalInfoId
              and o.status = com.yxtech.smartlife.entity.RentalOrder$OrderStatus.ACTIVE
            order by o.startDate asc, o.createdAt asc
            """)
    List<RentalOrder> findActiveOrders(@Param("rentalInfoId") Long rentalInfoId);
}
