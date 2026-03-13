package com.yxtech.smartlife.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.RentalConversation;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.RentalMessage;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.repository.RentalConversationRepository;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.RentalMessageRepository;
import com.yxtech.smartlife.repository.RentalOrderRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.impl.RentalTradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalTradeServiceImplTest {

    @Mock
    private RentalConversationRepository rentalConversationRepository;

    @Mock
    private RentalInfoRepository rentalInfoRepository;

    @Mock
    private RentalMessageRepository rentalMessageRepository;

    @Mock
    private RentalOrderRepository rentalOrderRepository;

    @Mock
    private UserRepository userRepository;

    private RentalTradeServiceImpl rentalTradeService;

    @BeforeEach
    void setUp() {
        rentalTradeService = new RentalTradeServiceImpl(
                rentalConversationRepository,
                rentalInfoRepository,
                rentalMessageRepository,
                rentalOrderRepository,
                userRepository,
                new ObjectMapper(),
                Clock.fixed(Instant.parse("2026-03-12T00:00:00Z"), ZoneId.of("Asia/Shanghai"))
        );
    }

    @Test
    void createOrderShouldRejectOverlappingDateRange() {
        RentalConversation conversation = buildConversation();
        RentalInfo rentalInfo = buildRental(RentalInfo.RentalStatus.APPROVED);

        when(rentalConversationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(conversation));
        when(rentalInfoRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(rentalInfo));
        when(rentalOrderRepository.findOverlappingOrders(
                10L,
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 4, 20),
                java.util.EnumSet.of(
                        RentalOrder.OrderStatus.PENDING_CONFIRMATION,
                        RentalOrder.OrderStatus.ACTIVE,
                        RentalOrder.OrderStatus.CANCEL_PENDING
                )
        )).thenReturn(List.of(buildOrder(RentalOrder.OrderStatus.ACTIVE)));

        assertThrows(IllegalArgumentException.class,
                () -> rentalTradeService.createOrder(1L, 2L, LocalDate.of(2026, 3, 20), LocalDate.of(2026, 4, 20)));
    }

    @Test
    void acceptOrderShouldMarkRentalAsRented() {
        RentalOrder order = buildOrder(RentalOrder.OrderStatus.PENDING_CONFIRMATION);
        RentalConversation conversation = buildConversation();
        RentalInfo rentalInfo = buildRental(RentalInfo.RentalStatus.APPROVED);

        when(rentalOrderRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(order));
        when(rentalConversationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(conversation));
        when(rentalInfoRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(rentalInfo));
        when(rentalOrderRepository.findOverlappingOrders(
                10L,
                order.getStartDate(),
                order.getEndDate(),
                java.util.EnumSet.of(
                        RentalOrder.OrderStatus.PENDING_CONFIRMATION,
                        RentalOrder.OrderStatus.ACTIVE,
                        RentalOrder.OrderStatus.CANCEL_PENDING
                )
        )).thenReturn(List.of(order));
        when(rentalOrderRepository.save(any(RentalOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalInfoRepository.save(any(RentalInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalOrderRepository.findActiveOrders(10L)).thenReturn(List.of(order));
        when(rentalMessageRepository.save(any(RentalMessage.class))).thenAnswer(invocation -> {
            RentalMessage message = invocation.getArgument(0);
            message.setId(200L);
            return message;
        });
        when(rentalConversationRepository.save(any(RentalConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalOrder accepted = rentalTradeService.acceptOrder(100L, 1L);

        assertEquals(RentalOrder.OrderStatus.ACTIVE, accepted.getStatus());
        assertEquals(RentalInfo.RentalStatus.RENTED, rentalInfo.getStatus());
        assertEquals(order.getStartDate(), rentalInfo.getRentStartDate());
        assertEquals(order.getEndDate(), rentalInfo.getRentEndDate());
        verify(rentalMessageRepository).save(any(RentalMessage.class));
    }

    @Test
    void confirmCancelShouldRestoreRentalAvailabilityAfterBothConfirm() {
        RentalOrder order = buildOrder(RentalOrder.OrderStatus.CANCEL_PENDING);
        order.setLandlordCancelConfirmed(true);
        order.setTenantCancelConfirmed(false);
        order.setCancelRequestedBy(1L);
        RentalConversation conversation = buildConversation();
        RentalInfo rentalInfo = buildRental(RentalInfo.RentalStatus.RENTED);

        when(rentalOrderRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(order));
        when(rentalConversationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(conversation));
        when(rentalInfoRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(rentalInfo));
        when(rentalOrderRepository.save(any(RentalOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalOrderRepository.findActiveOrders(10L)).thenReturn(List.of());
        when(rentalInfoRepository.save(any(RentalInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalMessageRepository.save(any(RentalMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalConversationRepository.save(any(RentalConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalOrder canceled = rentalTradeService.confirmCancel(100L, 2L);

        assertEquals(RentalOrder.OrderStatus.CANCELED, canceled.getStatus());
        assertEquals(RentalInfo.RentalStatus.APPROVED, rentalInfo.getStatus());
        assertEquals(null, rentalInfo.getRentStartDate());
        assertEquals(null, rentalInfo.getRentEndDate());
    }

    @Test
    void adminCancelShouldWriteSystemMessage() {
        RentalOrder order = buildOrder(RentalOrder.OrderStatus.ACTIVE);
        RentalConversation conversation = buildConversation();
        RentalInfo rentalInfo = buildRental(RentalInfo.RentalStatus.RENTED);

        when(rentalOrderRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(order));
        when(rentalConversationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(conversation));
        when(rentalInfoRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(rentalInfo));
        when(rentalOrderRepository.save(any(RentalOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalOrderRepository.findActiveOrders(10L)).thenReturn(List.of());
        when(rentalInfoRepository.save(any(RentalInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalMessageRepository.save(any(RentalMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalConversationRepository.save(any(RentalConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rentalTradeService.adminCancelOrder(100L, 999L, "管理员取消");

        ArgumentCaptor<RentalMessage> messageCaptor = ArgumentCaptor.forClass(RentalMessage.class);
        verify(rentalMessageRepository, times(1)).save(messageCaptor.capture());
        assertEquals(RentalMessage.MessageType.SYSTEM, messageCaptor.getValue().getMessageType());
        assertEquals("管理员已取消当前订单。", messageCaptor.getValue().getContent());
    }

    private RentalConversation buildConversation() {
        RentalConversation conversation = new RentalConversation();
        conversation.setId(1L);
        conversation.setRentalInfoId(10L);
        conversation.setLandlordUserId(1L);
        conversation.setTenantUserId(2L);
        return conversation;
    }

    private RentalInfo buildRental(RentalInfo.RentalStatus status) {
        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(10L);
        rentalInfo.setPublisherUserId(1L);
        rentalInfo.setRentalType(RentalInfo.RentalType.HOUSE);
        rentalInfo.setTitle("精装两居");
        rentalInfo.setDescription("近地铁");
        rentalInfo.setPrice(BigDecimal.valueOf(3200));
        rentalInfo.setContactName("房东");
        rentalInfo.setContactPhone("13800000000");
        rentalInfo.setCity("杭州");
        rentalInfo.setDistrict("滨江区");
        rentalInfo.setStreet("长河街道");
        rentalInfo.setCommunityName("卓悦华庭");
        rentalInfo.setStatus(status);
        return rentalInfo;
    }

    private RentalOrder buildOrder(RentalOrder.OrderStatus status) {
        RentalOrder order = new RentalOrder();
        order.setId(100L);
        order.setConversationId(1L);
        order.setRentalInfoId(10L);
        order.setLandlordUserId(1L);
        order.setTenantUserId(2L);
        order.setStartDate(LocalDate.of(2026, 3, 20));
        order.setEndDate(LocalDate.of(2026, 4, 20));
        order.setStatus(status);
        return order;
    }
}
