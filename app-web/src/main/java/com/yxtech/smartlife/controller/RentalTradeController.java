package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.ConversationListRequest;
import com.yxtech.smartlife.dto.CreateRentalOrderRequest;
import com.yxtech.smartlife.dto.MarkReadRequest;
import com.yxtech.smartlife.dto.OpenConversationRequest;
import com.yxtech.smartlife.dto.OrderActionRequest;
import com.yxtech.smartlife.dto.RenewRentalOrderRequest;
import com.yxtech.smartlife.dto.RentalConversationDetailDTO;
import com.yxtech.smartlife.dto.RentalConversationSummaryDTO;
import com.yxtech.smartlife.dto.RentalMessageDTO;
import com.yxtech.smartlife.dto.RentalOrderDTO;
import com.yxtech.smartlife.dto.SendConversationMessageRequest;
import com.yxtech.smartlife.dto.UnreadCountRequest;
import com.yxtech.smartlife.entity.RentalConversation;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.service.RentalTradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RentalTradeController {

    private final RentalTradeService rentalTradeService;
    private final ObjectMapper objectMapper;

    @PostMapping("/rentals/{rentalId}/conversation")
    public Result<Long> openConversation(
            @PathVariable("rentalId") Long rentalId,
            @Valid @RequestBody OpenConversationRequest request
    ) {
        RentalConversation conversation = rentalTradeService.openConversation(rentalId, request.getUserId());
        return Result.success(conversation.getId());
    }

    @GetMapping("/rental-conversations")
    public Result<List<RentalConversationSummaryDTO>> getConversationList(@RequestParam("userId") Long userId) {
        return Result.success(rentalTradeService.listUserConversations(userId).stream()
                .map(aggregate -> RentalConversationSummaryDTO.fromAggregate(aggregate, userId))
                .toList());
    }

    @GetMapping("/rental-conversations/{conversationId}")
    public Result<RentalConversationDetailDTO> getConversationDetail(
            @PathVariable("conversationId") Long conversationId,
            @RequestParam("userId") Long userId
    ) {
        return Result.success(RentalConversationDetailDTO.fromAggregate(
                rentalTradeService.getConversationDetail(conversationId, userId),
                userId,
                objectMapper
        ));
    }

    @PostMapping("/rental-conversations/{conversationId}/messages")
    public Result<RentalMessageDTO> sendTextMessage(
            @PathVariable("conversationId") Long conversationId,
            @Valid @RequestBody SendConversationMessageRequest request
    ) {
        rentalTradeService.sendTextMessage(conversationId, request.getUserId(), request.getContent());
        RentalConversationDetailDTO detailDTO = RentalConversationDetailDTO.fromAggregate(
                rentalTradeService.getConversationDetail(conversationId, request.getUserId()),
                request.getUserId(),
                objectMapper
        );
        return Result.success(detailDTO.getMessages().get(detailDTO.getMessages().size() - 1));
    }

    @PostMapping("/rental-conversations/{conversationId}/orders")
    public Result<RentalOrderDTO> createOrder(
            @PathVariable("conversationId") Long conversationId,
            @Valid @RequestBody CreateRentalOrderRequest request
    ) {
        RentalOrder order = rentalTradeService.createOrder(
                conversationId,
                request.getUserId(),
                request.getStartDate(),
                request.getEndDate()
        );
        RentalConversationDetailDTO detailDTO = RentalConversationDetailDTO.fromAggregate(
                rentalTradeService.getConversationDetail(conversationId, request.getUserId()),
                request.getUserId(),
                objectMapper
        );
        RentalOrderDTO dto = detailDTO.getOrders().stream()
                .filter(item -> item.getId().equals(order.getId()))
                .findFirst()
                .orElseGet(() -> RentalOrderDTO.fromEntity(order, detailDTO.getLandlordNickname(), detailDTO.getTenantNickname()));
        return Result.success(dto);
    }

    @PostMapping("/rental-orders/{orderId}/accept")
    public Result<RentalOrderDTO> acceptOrder(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody OrderActionRequest request
    ) {
        RentalOrder order = rentalTradeService.acceptOrder(orderId, request.getUserId());
        RentalConversationDetailDTO detailDTO = RentalConversationDetailDTO.fromAggregate(
                rentalTradeService.getConversationDetail(order.getConversationId(), request.getUserId()),
                request.getUserId(),
                objectMapper
        );
        return Result.success(detailDTO.getOrders().stream()
                .filter(item -> item.getId().equals(order.getId()))
                .findFirst()
                .orElseGet(() -> RentalOrderDTO.fromEntity(order, detailDTO.getLandlordNickname(), detailDTO.getTenantNickname())));
    }

    @PostMapping("/rental-orders/{orderId}/cancel/request")
    public Result<RentalOrderDTO> requestCancel(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody OrderActionRequest request
    ) {
        RentalOrder order = rentalTradeService.requestCancel(orderId, request.getUserId(), request.getReason());
        RentalConversationDetailDTO detailDTO = RentalConversationDetailDTO.fromAggregate(
                rentalTradeService.getConversationDetail(order.getConversationId(), request.getUserId()),
                request.getUserId(),
                objectMapper
        );
        return Result.success(detailDTO.getOrders().stream()
                .filter(item -> item.getId().equals(order.getId()))
                .findFirst()
                .orElseGet(() -> RentalOrderDTO.fromEntity(order, detailDTO.getLandlordNickname(), detailDTO.getTenantNickname())));
    }

    @PostMapping("/rental-orders/{orderId}/cancel/confirm")
    public Result<RentalOrderDTO> confirmCancel(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody OrderActionRequest request
    ) {
        RentalOrder order = rentalTradeService.confirmCancel(orderId, request.getUserId());
        RentalConversationDetailDTO detailDTO = RentalConversationDetailDTO.fromAggregate(
                rentalTradeService.getConversationDetail(order.getConversationId(), request.getUserId()),
                request.getUserId(),
                objectMapper
        );
        return Result.success(detailDTO.getOrders().stream()
                .filter(item -> item.getId().equals(order.getId()))
                .findFirst()
                .orElseGet(() -> RentalOrderDTO.fromEntity(order, detailDTO.getLandlordNickname(), detailDTO.getTenantNickname())));
    }

    @PostMapping("/rental-orders/{orderId}/renew")
    public Result<RentalOrderDTO> renewOrder(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody RenewRentalOrderRequest request
    ) {
        RentalOrder order = rentalTradeService.renewOrder(orderId, request.getUserId(), request.getStartDate(), request.getEndDate());
        RentalConversationDetailDTO detailDTO = RentalConversationDetailDTO.fromAggregate(
                rentalTradeService.getConversationDetail(order.getConversationId(), request.getUserId()),
                request.getUserId(),
                objectMapper
        );
        return Result.success(detailDTO.getOrders().stream()
                .filter(item -> item.getId().equals(order.getId()))
                .findFirst()
                .orElseGet(() -> RentalOrderDTO.fromEntity(order, detailDTO.getLandlordNickname(), detailDTO.getTenantNickname())));
    }

    @PostMapping("/messages/unread-count")
    public Result<Long> getUnreadCount(@Valid @RequestBody UnreadCountRequest request) {
        return Result.success(rentalTradeService.countUserUnreadMessages(request.getUserId()));
    }

    @PostMapping("/rental-conversations/list")
    public Result<List<RentalConversationSummaryDTO>> getConversationListPost(@Valid @RequestBody ConversationListRequest request) {
        Long userId = request.getUserId();
        return Result.success(rentalTradeService.listUserConversations(userId).stream()
                .map(aggregate -> {
                    RentalConversationSummaryDTO dto = RentalConversationSummaryDTO.fromAggregate(aggregate, userId);
                    dto.setUnreadCount(rentalTradeService.countConversationUnreadMessages(aggregate.conversation().getId(), userId));
                    return dto;
                })
                .toList());
    }

    @PostMapping("/rental-conversations/mark-read")
    public Result<Void> markConversationAsRead(@Valid @RequestBody MarkReadRequest request) {
        rentalTradeService.markConversationAsRead(request.getConversationId(), request.getUserId());
        return Result.success();
    }
}
