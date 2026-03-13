package com.yxtech.smartlife.dto;

import com.yxtech.smartlife.entity.RentalConversation;
import com.yxtech.smartlife.entity.RentalMessage;
import com.yxtech.smartlife.service.model.RentalConversationSummaryAggregate;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class RentalConversationSummaryDTO {

    private Long id;
    private Long rentalInfoId;
    private String rentalTitle;
    private String rentalDescription;
    private String locationText;
    private String rentalType;
    private String rentalStatus;
    private LocalDate rentStartDate;
    private LocalDate rentEndDate;
    private Long landlordUserId;
    private Long tenantUserId;
    private Long counterpartUserId;
    private String counterpartNickname;
    private RentalConversation.ConversationStatus status;
    private String lastMessagePreview;
    private RentalMessage.MessageType lastMessageType;
    private LocalDateTime lastMessageAt;
    private RentalOrderDTO latestOrder;
    private long unreadCount;

    public static RentalConversationSummaryDTO fromAggregate(RentalConversationSummaryAggregate aggregate, Long currentUserId) {
        RentalConversationSummaryDTO dto = new RentalConversationSummaryDTO();
        dto.setId(aggregate.conversation().getId());
        dto.setRentalInfoId(aggregate.rentalInfo().getId());
        dto.setRentalTitle(aggregate.rentalInfo().getTitle());
        dto.setRentalDescription(aggregate.rentalInfo().getDescription());
        dto.setLocationText(String.join(
                " / ",
                java.util.List.of(
                        defaultText(aggregate.rentalInfo().getCity()),
                        defaultText(aggregate.rentalInfo().getDistrict()),
                        defaultText(aggregate.rentalInfo().getStreet()),
                        defaultText(aggregate.rentalInfo().getCommunityName())
                ).stream().filter(value -> !value.isBlank()).toList()
        ));
        dto.setRentalType(aggregate.rentalInfo().getRentalType().name());
        dto.setRentalStatus(aggregate.rentalInfo().getStatus().name());
        dto.setRentStartDate(aggregate.rentalInfo().getRentStartDate());
        dto.setRentEndDate(aggregate.rentalInfo().getRentEndDate());
        dto.setLandlordUserId(aggregate.conversation().getLandlordUserId());
        dto.setTenantUserId(aggregate.conversation().getTenantUserId());
        dto.setStatus(aggregate.conversation().getStatus());
        if (aggregate.lastMessage() != null) {
            dto.setLastMessagePreview(aggregate.lastMessage().getContent());
            dto.setLastMessageType(aggregate.lastMessage().getMessageType());
            dto.setLastMessageAt(aggregate.lastMessage().getCreatedAt());
        }
        if (aggregate.latestOrder() != null) {
            dto.setLatestOrder(RentalOrderDTO.fromEntity(
                    aggregate.latestOrder(),
                    aggregate.landlord().getNickname(),
                    aggregate.tenant().getNickname()
            ));
        }
        if (Objects.equals(currentUserId, aggregate.landlord().getId())) {
            dto.setCounterpartUserId(aggregate.tenant().getId());
            dto.setCounterpartNickname(aggregate.tenant().getNickname());
        } else {
            dto.setCounterpartUserId(aggregate.landlord().getId());
            dto.setCounterpartNickname(aggregate.landlord().getNickname());
        }
        return dto;
    }

    private static String defaultText(String value) {
        return value == null ? "" : value;
    }
}
