package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.RentalConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentalConversationRepository extends JpaRepository<RentalConversation, Long> {

    Optional<RentalConversation> findByIdAndDeletedFalse(Long id);

    Optional<RentalConversation> findByRentalInfoIdAndLandlordUserIdAndTenantUserIdAndDeletedFalse(
            Long rentalInfoId,
            Long landlordUserId,
            Long tenantUserId
    );

    List<RentalConversation> findByTenantUserIdAndDeletedFalseOrderByLastMessageAtDescCreatedAtDesc(Long tenantUserId);

    List<RentalConversation> findByLandlordUserIdAndDeletedFalseOrderByLastMessageAtDescCreatedAtDesc(Long landlordUserId);

    List<RentalConversation> findByRentalInfoIdAndLandlordUserIdAndDeletedFalseOrderByLastMessageAtDescCreatedAtDesc(
            Long rentalInfoId,
            Long landlordUserId
    );

    @Query("""
            select c from RentalConversation c
            where c.deleted = false
              and (c.tenantUserId = :userId or c.landlordUserId = :userId)
            order by coalesce(c.lastMessageAt, c.createdAt) desc, c.createdAt desc
            """)
    List<RentalConversation> findParticipantConversations(@Param("userId") Long userId);
}
