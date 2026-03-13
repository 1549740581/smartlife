package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.RentalMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RentalMessageRepository extends JpaRepository<RentalMessage, Long> {

    List<RentalMessage> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(Long conversationId);

    RentalMessage findFirstByConversationIdAndDeletedFalseOrderByCreatedAtDesc(Long conversationId);

    long countByConversationIdAndReceiverUserIdAndReadAtIsNullAndDeletedFalse(Long conversationId, Long receiverUserId);

    long countByReceiverUserIdAndReadAtIsNullAndDeletedFalse(Long receiverUserId);

    @Modifying
    @Query("UPDATE RentalMessage m SET m.readAt = CURRENT_TIMESTAMP WHERE m.conversationId = :conversationId AND m.receiverUserId = :receiverUserId AND m.readAt IS NULL AND m.deleted = false")
    int markConversationMessagesAsRead(@Param("conversationId") Long conversationId, @Param("receiverUserId") Long receiverUserId);
}
