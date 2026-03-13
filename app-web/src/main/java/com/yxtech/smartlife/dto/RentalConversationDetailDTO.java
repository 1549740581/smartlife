package com.yxtech.smartlife.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.service.model.RentalConversationAggregate;
import lombok.Data;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class RentalConversationDetailDTO {

    private Long id;
    private String status;
    private String currentUserRole;
    private Long landlordUserId;
    private String landlordNickname;
    private Long tenantUserId;
    private String tenantNickname;
    private Long counterpartUserId;
    private String counterpartNickname;
    private RentalDTO rental;
    private List<RentalOrderDTO> orders;
    private List<RentalMessageDTO> messages;

    public static RentalConversationDetailDTO fromAggregate(
            RentalConversationAggregate aggregate,
            Long currentUserId,
            ObjectMapper objectMapper
    ) {
        RentalConversationDetailDTO dto = new RentalConversationDetailDTO();
        dto.setId(aggregate.conversation().getId());
        dto.setStatus(aggregate.conversation().getStatus().name());
        dto.setLandlordUserId(aggregate.landlord().getId());
        dto.setLandlordNickname(aggregate.landlord().getNickname());
        dto.setTenantUserId(aggregate.tenant().getId());
        dto.setTenantNickname(aggregate.tenant().getNickname());
        dto.setRental(RentalDTO.fromEntity(aggregate.rentalInfo(), objectMapper));
        dto.setCurrentUserRole(Objects.equals(currentUserId, aggregate.landlord().getId()) ? "LANDLORD" : "TENANT");
        if (Objects.equals(currentUserId, aggregate.landlord().getId())) {
            dto.setCounterpartUserId(aggregate.tenant().getId());
            dto.setCounterpartNickname(aggregate.tenant().getNickname());
        } else {
            dto.setCounterpartUserId(aggregate.landlord().getId());
            dto.setCounterpartNickname(aggregate.landlord().getNickname());
        }

        Map<Long, String> nicknameMap = new LinkedHashMap<>();
        nicknameMap.put(aggregate.landlord().getId(), defaultNickname(aggregate.landlord().getNickname(), "房东"));
        nicknameMap.put(aggregate.tenant().getId(), defaultNickname(aggregate.tenant().getNickname(), "租客"));
        Map<Long, RentalOrder> orderMap = aggregate.orders().stream()
                .collect(Collectors.toMap(RentalOrder::getId, Function.identity(), (left, right) -> right));
        dto.setOrders(aggregate.orders().stream()
                .map(order -> RentalOrderDTO.fromEntity(
                        order,
                        defaultNickname(aggregate.landlord().getNickname(), "房东"),
                        defaultNickname(aggregate.tenant().getNickname(), "租客")
                ))
                .toList());
        dto.setMessages(aggregate.messages().stream()
                .map(message -> RentalMessageDTO.fromEntity(
                        message,
                        nicknameMap,
                        orderMap,
                        defaultNickname(aggregate.landlord().getNickname(), "房东"),
                        defaultNickname(aggregate.tenant().getNickname(), "租客")
                ))
                .toList());
        return dto;
    }

    private static String defaultNickname(String nickname, String fallback) {
        return (nickname == null || nickname.isBlank()) ? fallback : nickname;
    }
}
