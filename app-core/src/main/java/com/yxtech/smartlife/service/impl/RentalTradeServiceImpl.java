package com.yxtech.smartlife.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.RentalConversation;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.RentalMessage;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.exception.NotFoundException;
import com.yxtech.smartlife.repository.RentalConversationRepository;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.RentalMessageRepository;
import com.yxtech.smartlife.repository.RentalOrderRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.RentalTradeService;
import com.yxtech.smartlife.service.model.RentalConversationAggregate;
import com.yxtech.smartlife.service.model.RentalConversationSummaryAggregate;
import com.yxtech.smartlife.service.model.RentalOrderAggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class RentalTradeServiceImpl implements RentalTradeService {

    private static final Set<RentalOrder.OrderStatus> CONFLICT_STATUSES = EnumSet.of(
            RentalOrder.OrderStatus.PENDING_CONFIRMATION,
            RentalOrder.OrderStatus.ACTIVE,
            RentalOrder.OrderStatus.CANCEL_PENDING
    );

    private final RentalConversationRepository rentalConversationRepository;
    private final RentalInfoRepository rentalInfoRepository;
    private final RentalMessageRepository rentalMessageRepository;
    private final RentalOrderRepository rentalOrderRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public RentalTradeServiceImpl(
            RentalConversationRepository rentalConversationRepository,
            RentalInfoRepository rentalInfoRepository,
            RentalMessageRepository rentalMessageRepository,
            RentalOrderRepository rentalOrderRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            @Qualifier("systemClock") Clock clock
    ) {
        this.rentalConversationRepository = rentalConversationRepository;
        this.rentalInfoRepository = rentalInfoRepository;
        this.rentalMessageRepository = rentalMessageRepository;
        this.rentalOrderRepository = rentalOrderRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RentalConversation openConversation(Long rentalInfoId, Long tenantUserId) {
        RentalInfo rentalInfo = loadRental(rentalInfoId);
        User tenant = loadUser(tenantUserId);
        RentalConversation existingConversation = rentalConversationRepository
                .findByRentalInfoIdAndLandlordUserIdAndTenantUserIdAndDeletedFalse(
                        rentalInfoId,
                        rentalInfo.getPublisherUserId(),
                        tenantUserId
                )
                .orElse(null);

        if (rentalInfo.getRentalType() == RentalInfo.RentalType.ITEM) {
            throw new IllegalArgumentException("only house and parking support rental communication");
        }
        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.APPROVED
                && !(rentalInfo.getStatus() == RentalInfo.RentalStatus.RENTED && existingConversation != null)) {
            throw new IllegalArgumentException("current rental does not support communication");
        }
        if (Objects.equals(rentalInfo.getPublisherUserId(), tenant.getId())) {
            throw new IllegalArgumentException("publisher cannot open conversation with self");
        }

        return java.util.Optional.ofNullable(existingConversation)
                .map(existing -> {
                    existing.setStatus(RentalConversation.ConversationStatus.OPEN);
                    return rentalConversationRepository.save(existing);
                })
                .orElseGet(() -> {
                    RentalConversation conversation = new RentalConversation();
                    conversation.setRentalInfoId(rentalInfoId);
                    conversation.setLandlordUserId(rentalInfo.getPublisherUserId());
                    conversation.setTenantUserId(tenantUserId);
                    conversation.setStatus(RentalConversation.ConversationStatus.OPEN);
                    return rentalConversationRepository.save(conversation);
                });
    }

    @Override
    public List<RentalConversationSummaryAggregate> listUserConversations(Long userId) {
        loadUser(userId);
        return rentalConversationRepository.findParticipantConversations(userId).stream()
                .map(this::buildConversationSummary)
                .toList();
    }

    @Override
    public RentalConversationAggregate getConversationDetail(Long conversationId, Long userId) {
        RentalConversation conversation = loadConversation(conversationId);
        ensureParticipant(conversation, userId);

        RentalInfo rentalInfo = loadRental(conversation.getRentalInfoId());
        User landlord = loadUser(conversation.getLandlordUserId());
        User tenant = loadUser(conversation.getTenantUserId());
        List<RentalMessage> messages = rentalMessageRepository.findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(conversationId);
        List<RentalOrder> orders = rentalOrderRepository.findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(conversationId).stream()
                .sorted(Comparator.comparing(RentalOrder::getCreatedAt).reversed())
                .toList();
        return new RentalConversationAggregate(conversation, rentalInfo, landlord, tenant, messages, orders);
    }

    @Override
    @Transactional
    public RentalMessage sendTextMessage(Long conversationId, Long senderUserId, String content) {
        RentalConversation conversation = loadConversation(conversationId);
        ensureParticipant(conversation, senderUserId);

        String messageContent = trimToNull(content);
        if (!StringUtils.hasText(messageContent)) {
            throw new IllegalArgumentException("message content must not be blank");
        }
        if (messageContent.length() > 1000) {
            throw new IllegalArgumentException("message content exceeds limit");
        }
        return saveMessage(
                conversation,
                senderUserId,
                resolveReceiverUserId(conversation, senderUserId),
                RentalMessage.MessageType.TEXT,
                messageContent,
                null,
                null
        );
    }

    @Override
    @Transactional
    public RentalOrder createOrder(Long conversationId, Long tenantUserId, LocalDate startDate, LocalDate endDate) {
        RentalConversation conversation = loadConversation(conversationId);
        if (!Objects.equals(conversation.getTenantUserId(), tenantUserId)) {
            throw new IllegalArgumentException("only tenant can create rental order");
        }

        RentalInfo rentalInfo = loadRental(conversation.getRentalInfoId());
        if (rentalInfo.getRentalType() == RentalInfo.RentalType.ITEM) {
            throw new IllegalArgumentException("current rental does not support ordering");
        }
        validateDateRange(startDate, endDate);
        ensureNoConflict(rentalInfo.getId(), startDate, endDate, null);

        RentalOrder order = new RentalOrder();
        order.setConversationId(conversationId);
        order.setRentalInfoId(rentalInfo.getId());
        order.setLandlordUserId(conversation.getLandlordUserId());
        order.setTenantUserId(conversation.getTenantUserId());
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setStatus(RentalOrder.OrderStatus.PENDING_CONFIRMATION);
        RentalOrder saved = rentalOrderRepository.save(order);

        saveMessage(
                conversation,
                tenantUserId,
                conversation.getLandlordUserId(),
                RentalMessage.MessageType.ORDER_CARD,
                buildOrderCardContent(saved),
                saved.getId(),
                buildOrderMetadata(saved)
        );
        return saved;
    }

    @Override
    @Transactional
    public RentalOrder acceptOrder(Long orderId, Long landlordUserId) {
        RentalOrder order = loadOrder(orderId);
        if (!Objects.equals(order.getLandlordUserId(), landlordUserId)) {
            throw new IllegalArgumentException("only landlord can accept order");
        }
        if (order.getStatus() != RentalOrder.OrderStatus.PENDING_CONFIRMATION) {
            throw new IllegalArgumentException("only pending order can be accepted");
        }

        ensureNoConflict(order.getRentalInfoId(), order.getStartDate(), order.getEndDate(), order.getId());
        order.setStatus(RentalOrder.OrderStatus.ACTIVE);
        RentalOrder saved = rentalOrderRepository.save(order);
        refreshRentalAvailability(order.getRentalInfoId());
        saveSystemMessage(
                loadConversation(order.getConversationId()),
                saved.getId(),
                "房东已确认租期，订单生效。"
        );
        return saved;
    }

    @Override
    @Transactional
    public RentalOrder requestCancel(Long orderId, Long userId, String reason) {
        RentalOrder order = loadOrder(orderId);
        ensureParticipant(order, userId);
        String normalizedReason = trimToNull(reason);

        if (order.getStatus() == RentalOrder.OrderStatus.PENDING_CONFIRMATION) {
            order.setStatus(RentalOrder.OrderStatus.CANCELED);
            order.setCancelRequestedBy(userId);
            order.setCancelReason(normalizedReason);
            order.setCancelRequestedAt(now());
            order.setLandlordCancelConfirmed(Objects.equals(order.getLandlordUserId(), userId));
            order.setTenantCancelConfirmed(Objects.equals(order.getTenantUserId(), userId));
            RentalOrder saved = rentalOrderRepository.save(order);
            saveSystemMessage(
                    loadConversation(order.getConversationId()),
                    saved.getId(),
                    buildCancelMessage(userId, order, "已撤回租期申请。")
            );
            return saved;
        }

        if (order.getStatus() != RentalOrder.OrderStatus.ACTIVE) {
            if (order.getStatus() == RentalOrder.OrderStatus.CANCEL_PENDING) {
                throw new IllegalArgumentException("cancellation is already pending confirmation");
            }
            throw new IllegalArgumentException("current order cannot request cancellation");
        }

        order.setStatus(RentalOrder.OrderStatus.CANCEL_PENDING);
        order.setCancelRequestedBy(userId);
        order.setCancelReason(normalizedReason);
        order.setCancelRequestedAt(now());
        order.setLandlordCancelConfirmed(Objects.equals(order.getLandlordUserId(), userId));
        order.setTenantCancelConfirmed(Objects.equals(order.getTenantUserId(), userId));
        RentalOrder saved = rentalOrderRepository.save(order);
        saveSystemMessage(
                loadConversation(order.getConversationId()),
                saved.getId(),
                buildCancelMessage(userId, order, "发起了取消申请，等待对方确认。")
        );
        return saved;
    }

    @Override
    @Transactional
    public RentalOrder confirmCancel(Long orderId, Long userId) {
        RentalOrder order = loadOrder(orderId);
        ensureParticipant(order, userId);
        if (order.getStatus() != RentalOrder.OrderStatus.CANCEL_PENDING) {
            throw new IllegalArgumentException("current order is not waiting for cancellation confirmation");
        }

        if (Objects.equals(order.getLandlordUserId(), userId)) {
            order.setLandlordCancelConfirmed(true);
        }
        if (Objects.equals(order.getTenantUserId(), userId)) {
            order.setTenantCancelConfirmed(true);
        }

        if (Boolean.TRUE.equals(order.getLandlordCancelConfirmed())
                && Boolean.TRUE.equals(order.getTenantCancelConfirmed())) {
            order.setStatus(RentalOrder.OrderStatus.CANCELED);
            RentalOrder saved = rentalOrderRepository.save(order);
            refreshRentalAvailability(order.getRentalInfoId());
            saveSystemMessage(
                    loadConversation(order.getConversationId()),
                    saved.getId(),
                    "取消申请已由双方确认，房源重新公开展示。"
            );
            return saved;
        }

        RentalOrder saved = rentalOrderRepository.save(order);
        saveSystemMessage(
                loadConversation(order.getConversationId()),
                saved.getId(),
                buildUserLabel(userId, order) + "已确认取消申请，等待另一方确认。"
        );
        return saved;
    }

    @Override
    @Transactional
    public RentalOrder renewOrder(Long orderId, Long userId, LocalDate startDate, LocalDate endDate) {
        RentalOrder sourceOrder = loadOrder(orderId);
        ensureParticipant(sourceOrder, userId);

        User user = loadUser(userId);
        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new IllegalArgumentException("locked user cannot renew order");
        }

        RentalInfo rentalInfo = loadRental(sourceOrder.getRentalInfoId());
        if (rentalInfo.getStatus() == RentalInfo.RentalStatus.OFFLINE) {
            throw new IllegalArgumentException("rental has been taken offline, cannot renew");
        }

        User landlord = loadUser(sourceOrder.getLandlordUserId());
        if (landlord.getStatus() == User.UserStatus.LOCKED) {
            throw new IllegalArgumentException("landlord account is locked, cannot renew");
        }

        if (sourceOrder.getStatus() != RentalOrder.OrderStatus.ACTIVE
                && sourceOrder.getStatus() != RentalOrder.OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("only active or completed order can be renewed");
        }
        validateDateRange(startDate, endDate);
        if (!startDate.isAfter(sourceOrder.getEndDate())) {
            throw new IllegalArgumentException("renewal start date must be after current order end date");
        }

        ensureNoConflict(sourceOrder.getRentalInfoId(), startDate, endDate, null);

        RentalOrder renewalOrder = new RentalOrder();
        renewalOrder.setConversationId(sourceOrder.getConversationId());
        renewalOrder.setRentalInfoId(sourceOrder.getRentalInfoId());
        renewalOrder.setLandlordUserId(sourceOrder.getLandlordUserId());
        renewalOrder.setTenantUserId(sourceOrder.getTenantUserId());
        renewalOrder.setStartDate(startDate);
        renewalOrder.setEndDate(endDate);
        renewalOrder.setStatus(RentalOrder.OrderStatus.PENDING_CONFIRMATION);
        renewalOrder.setRenewalFromOrderId(sourceOrder.getId());
        RentalOrder saved = rentalOrderRepository.save(renewalOrder);

        saveMessage(
                loadConversation(sourceOrder.getConversationId()),
                userId,
                resolveReceiverUserId(loadConversation(sourceOrder.getConversationId()), userId),
                RentalMessage.MessageType.ORDER_CARD,
                "发起续约申请：" + buildDateRangeText(saved.getStartDate(), saved.getEndDate()),
                saved.getId(),
                buildOrderMetadata(saved)
        );
        return saved;
    }

    @Override
    public List<RentalOrderAggregate> listAdminOrders() {
        return rentalOrderRepository.findByDeletedFalseOrderByCreatedAtDesc().stream()
                .map(this::buildOrderAggregate)
                .toList();
    }

    @Override
    @Transactional
    public RentalOrder adminCancelOrder(Long orderId, Long adminId, String reason) {
        RentalOrder order = loadOrder(orderId);
        if (order.getStatus() == RentalOrder.OrderStatus.CANCELED
                || order.getStatus() == RentalOrder.OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("current order cannot be canceled by admin");
        }

        order.setStatus(RentalOrder.OrderStatus.CANCELED);
        order.setCancelRequestedBy(adminId);
        order.setCancelReason(trimToNull(reason));
        order.setCancelRequestedAt(now());
        order.setLandlordCancelConfirmed(true);
        order.setTenantCancelConfirmed(true);
        RentalOrder saved = rentalOrderRepository.save(order);
        refreshRentalAvailability(order.getRentalInfoId());
        saveSystemMessage(
                loadConversation(order.getConversationId()),
                saved.getId(),
                "管理员已取消当前订单。"
        );
        return saved;
    }

    @Override
    @Transactional
    public void sendExpirationReminders() {
        LocalDate reminderDate = today().plusDays(15);
        for (RentalOrder order : rentalOrderRepository.findOrdersNeedReminder(reminderDate)) {
            order.setReminderSentAt(now());
            RentalOrder saved = rentalOrderRepository.save(order);
            saveSystemMessage(
                    loadConversation(saved.getConversationId()),
                    saved.getId(),
                    "当前租期将在 15 天后到期，请双方及时确认是否续约。"
            );
        }
    }

    @Override
    @Transactional
    public void completeExpiredOrders() {
        for (RentalOrder order : rentalOrderRepository.findExpiredActiveOrders(today())) {
            order.setStatus(RentalOrder.OrderStatus.COMPLETED);
            RentalOrder saved = rentalOrderRepository.save(order);
            refreshRentalAvailability(order.getRentalInfoId());
            saveSystemMessage(
                    loadConversation(saved.getConversationId()),
                    saved.getId(),
                    "当前租期已到期，订单自动完成。"
            );
        }
    }

    private RentalConversationSummaryAggregate buildConversationSummary(RentalConversation conversation) {
        RentalInfo rentalInfo = loadRental(conversation.getRentalInfoId());
        User landlord = loadUser(conversation.getLandlordUserId());
        User tenant = loadUser(conversation.getTenantUserId());
        RentalMessage lastMessage = rentalMessageRepository.findFirstByConversationIdAndDeletedFalseOrderByCreatedAtDesc(conversation.getId());
        RentalOrder latestOrder = rentalOrderRepository.findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(conversation.getId()).stream()
                .max(Comparator.comparing(RentalOrder::getCreatedAt))
                .orElse(null);
        return new RentalConversationSummaryAggregate(conversation, rentalInfo, landlord, tenant, lastMessage, latestOrder);
    }

    private RentalOrderAggregate buildOrderAggregate(RentalOrder order) {
        return new RentalOrderAggregate(
                order,
                loadRental(order.getRentalInfoId()),
                loadUser(order.getLandlordUserId()),
                loadUser(order.getTenantUserId())
        );
    }

    private void refreshRentalAvailability(Long rentalInfoId) {
        RentalInfo rentalInfo = loadRental(rentalInfoId);
        List<RentalOrder> activeOrders = rentalOrderRepository.findActiveOrders(rentalInfoId);
        if (activeOrders.isEmpty()) {
            rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
            rentalInfo.setRentStartDate(null);
            rentalInfo.setRentEndDate(null);
            rentalInfoRepository.save(rentalInfo);
            return;
        }

        RentalOrder nearestOrder = activeOrders.stream()
                .min(Comparator.comparing(RentalOrder::getStartDate))
                .orElseThrow();
        rentalInfo.setStatus(RentalInfo.RentalStatus.RENTED);
        rentalInfo.setRentStartDate(nearestOrder.getStartDate());
        rentalInfo.setRentEndDate(nearestOrder.getEndDate());
        rentalInfoRepository.save(rentalInfo);
    }

    private void ensureNoConflict(Long rentalInfoId, LocalDate startDate, LocalDate endDate, Long excludeOrderId) {
        List<RentalOrder> conflictOrders = rentalOrderRepository.findOverlappingOrders(
                rentalInfoId,
                startDate,
                endDate,
                CONFLICT_STATUSES
        ).stream()
                .filter(order -> !Objects.equals(order.getId(), excludeOrderId))
                .toList();
        if (!conflictOrders.isEmpty()) {
            throw new IllegalArgumentException("selected date range conflicts with existing rental order");
        }
    }

    private RentalMessage saveSystemMessage(RentalConversation conversation, Long orderId, String content) {
        return saveMessage(
                conversation,
                null,
                null,
                RentalMessage.MessageType.SYSTEM,
                content,
                orderId,
                null
        );
    }

    private RentalMessage saveMessage(
            RentalConversation conversation,
            Long senderUserId,
            Long receiverUserId,
            RentalMessage.MessageType messageType,
            String content,
            Long orderId,
            String metadataJson
    ) {
        RentalMessage message = new RentalMessage();
        message.setConversationId(conversation.getId());
        message.setRentalInfoId(conversation.getRentalInfoId());
        message.setOrderId(orderId);
        message.setSenderUserId(senderUserId);
        message.setReceiverUserId(receiverUserId);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setMetadataJson(metadataJson);
        RentalMessage saved = rentalMessageRepository.save(message);
        conversation.setLastMessageAt(saved.getCreatedAt() == null ? now() : saved.getCreatedAt());
        rentalConversationRepository.save(conversation);
        return saved;
    }

    private String buildOrderMetadata(RentalOrder order) {
        try {
            java.util.Map<String, Object> metadata = new java.util.LinkedHashMap<>();
            metadata.put("startDate", order.getStartDate());
            metadata.put("endDate", order.getEndDate());
            metadata.put("status", order.getStatus());
            metadata.put("renewalFromOrderId", order.getRenewalFromOrderId());
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("order metadata serialization failed");
        }
    }

    private String buildOrderCardContent(RentalOrder order) {
        return "租期申请：" + buildDateRangeText(order.getStartDate(), order.getEndDate());
    }

    private String buildDateRangeText(LocalDate startDate, LocalDate endDate) {
        return startDate + " 至 " + endDate;
    }

    private String buildCancelMessage(Long userId, RentalOrder order, String suffix) {
        return buildUserLabel(userId, order) + suffix;
    }

    private String buildUserLabel(Long userId, RentalOrder order) {
        if (Objects.equals(order.getLandlordUserId(), userId)) {
            return "房东";
        }
        if (Objects.equals(order.getTenantUserId(), userId)) {
            return "租客";
        }
        return "用户";
    }

    private Long resolveReceiverUserId(RentalConversation conversation, Long senderUserId) {
        if (Objects.equals(conversation.getLandlordUserId(), senderUserId)) {
            return conversation.getTenantUserId();
        }
        return conversation.getLandlordUserId();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("start date and end date must not be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("end date must not be earlier than start date");
        }
        if (startDate.isBefore(today())) {
            throw new IllegalArgumentException("start date must not be earlier than today");
        }
    }

    private void ensureParticipant(RentalConversation conversation, Long userId) {
        if (!Objects.equals(conversation.getLandlordUserId(), userId)
                && !Objects.equals(conversation.getTenantUserId(), userId)) {
            throw new IllegalArgumentException("current user is not a participant of this conversation");
        }
    }

    private void ensureParticipant(RentalOrder order, Long userId) {
        if (!Objects.equals(order.getLandlordUserId(), userId)
                && !Objects.equals(order.getTenantUserId(), userId)) {
            throw new IllegalArgumentException("current user is not a participant of this order");
        }
    }

    private RentalConversation loadConversation(Long conversationId) {
        return rentalConversationRepository.findByIdAndDeletedFalse(conversationId)
                .orElseThrow(() -> new NotFoundException("conversation not found"));
    }

    private RentalOrder loadOrder(Long orderId) {
        return rentalOrderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new NotFoundException("rental order not found"));
    }

    private RentalInfo loadRental(Long rentalId) {
        return rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));
    }

    private User loadUser(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @Override
    public long countUserUnreadMessages(Long userId) {
        return rentalMessageRepository.countByReceiverUserIdAndReadAtIsNullAndDeletedFalse(userId);
    }

    @Override
    public long countConversationUnreadMessages(Long conversationId, Long userId) {
        return rentalMessageRepository.countByConversationIdAndReceiverUserIdAndReadAtIsNullAndDeletedFalse(conversationId, userId);
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long conversationId, Long userId) {
        rentalMessageRepository.markConversationMessagesAsRead(conversationId, userId);
    }
}
