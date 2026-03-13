package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.repository.RentalConversationRepository;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.RentalMessageRepository;
import com.yxtech.smartlife.repository.RentalOrderRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.support.AbstractContainerIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RentalTradeServiceContainerTest extends AbstractContainerIntegrationTest {

    @Autowired
    private RentalTradeService rentalTradeService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RentalInfoRepository rentalInfoRepository;

    @Autowired
    private RentalConversationRepository rentalConversationRepository;

    @Autowired
    private RentalOrderRepository rentalOrderRepository;

    @Autowired
    private RentalMessageRepository rentalMessageRepository;

    @BeforeEach
    void cleanUp() {
        rentalMessageRepository.deleteAllInBatch();
        rentalOrderRepository.deleteAllInBatch();
        rentalConversationRepository.deleteAllInBatch();
        rentalInfoRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void createAndAcceptOrderShouldPersistTradeFlow() {
        User landlord = createUser("landlord-a");
        User tenant = createUser("tenant-b");

        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setPublisherUserId(landlord.getId());
        rentalInfo.setRentalType(RentalInfo.RentalType.HOUSE);
        rentalInfo.setTitle("精装两居室");
        rentalInfo.setDescription("近地铁");
        rentalInfo.setPrice(BigDecimal.valueOf(4200));
        rentalInfo.setContactName("房东");
        rentalInfo.setContactPhone("13800000000");
        rentalInfo.setCity("杭州");
        rentalInfo.setDistrict("滨江区");
        rentalInfo.setStreet("长河街道");
        rentalInfo.setCommunityName("卓悦华庭");
        rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
        rentalInfo = rentalInfoRepository.save(rentalInfo);

        Long conversationId = rentalTradeService.openConversation(rentalInfo.getId(), tenant.getId()).getId();
        RentalOrder pendingOrder = rentalTradeService.createOrder(
                conversationId,
                tenant.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(31)
        );
        RentalOrder activeOrder = rentalTradeService.acceptOrder(pendingOrder.getId(), landlord.getId());

        RentalInfo persistedRental = rentalInfoRepository.findById(rentalInfo.getId()).orElseThrow();
        assertEquals(RentalOrder.OrderStatus.ACTIVE, activeOrder.getStatus());
        assertEquals(RentalInfo.RentalStatus.RENTED, persistedRental.getStatus());
        assertEquals(LocalDate.now().plusDays(1), persistedRental.getRentStartDate());
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setNickname(username);
        user.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
