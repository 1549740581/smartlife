package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.ReviewRecord;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.ReviewRecordRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.command.CreateRentalCommand;
import com.yxtech.smartlife.support.AbstractContainerIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RentalServiceContainerTest extends AbstractContainerIntegrationTest {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RentalInfoRepository rentalInfoRepository;

    @Autowired
    private ReviewRecordRepository reviewRecordRepository;

    @BeforeEach
    void cleanUp() {
        reviewRecordRepository.deleteAllInBatch();
        rentalInfoRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void createRentalShouldPersistPendingRentalIntoMysql() {
        User publisher = createUser("publisher-one");

        RentalInfo saved = rentalService.createRental(CreateRentalCommand.builder()
                .publisherUserId(publisher.getId())
                .rentalType(RentalInfo.RentalType.HOUSE)
                .title("精装两居室")
                .description("近地铁")
                .price(BigDecimal.valueOf(3500))
                .contactName("张三")
                .contactPhone("13800000000")
                .communityName("阳光小区")
                .imageUrls(List.of("https://img.test/a.jpg"))
                .build());

        RentalInfo persisted = rentalInfoRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(persisted.getId());
        assertEquals(RentalInfo.RentalStatus.PENDING, persisted.getStatus());
        assertEquals("[\"https://img.test/a.jpg\"]", persisted.getImageUrls());
    }

    @Test
    void reviewRentalShouldPersistReviewRecordIntoMysql() {
        User publisher = createUser("publisher-two");
        RentalInfo rentalInfo = rentalService.createRental(CreateRentalCommand.builder()
                .publisherUserId(publisher.getId())
                .rentalType(RentalInfo.RentalType.PARKING)
                .title("车位")
                .description("地库固定车位")
                .price(BigDecimal.valueOf(500))
                .contactName("李四")
                .contactPhone("13900000000")
                .imageUrls(List.of())
                .build());

        RentalInfo reviewed = rentalService.reviewRental(rentalInfo.getId(), 100L, true, "");

        List<ReviewRecord> records = reviewRecordRepository.findByRentalInfoIdOrderByCreatedAtDesc(rentalInfo.getId());
        assertEquals(RentalInfo.RentalStatus.APPROVED, reviewed.getStatus());
        assertFalse(records.isEmpty());
        assertEquals("APPROVE", records.get(0).getAction());
        assertEquals("APPROVED", records.get(0).getToStatus());
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
