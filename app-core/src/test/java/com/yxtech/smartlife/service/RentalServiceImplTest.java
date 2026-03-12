package com.yxtech.smartlife.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.ReviewRecord;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.ReviewRecordRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.command.CreateRentalCommand;
import com.yxtech.smartlife.service.impl.RentalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock
    private RentalInfoRepository rentalInfoRepository;

    @Mock
    private ReviewRecordRepository reviewRecordRepository;

    @Mock
    private UserRepository userRepository;

    private RentalServiceImpl rentalService;

    @BeforeEach
    void setUp() {
        rentalService = new RentalServiceImpl(
                rentalInfoRepository,
                reviewRecordRepository,
                userRepository,
                new ObjectMapper()
        );
    }

    @Test
    void createRentalShouldSavePendingRental() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(rentalInfoRepository.save(any(RentalInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalInfo rentalInfo = rentalService.createRental(CreateRentalCommand.builder()
                .publisherUserId(1L)
                .rentalType(RentalInfo.RentalType.HOUSE)
                .title("两居室")
                .description("近地铁")
                .price(BigDecimal.valueOf(3200))
                .contactName("张三")
                .contactPhone("13800000000")
                .imageUrls(List.of("https://img/a.jpg"))
                .build());

        assertEquals(RentalInfo.RentalStatus.PENDING, rentalInfo.getStatus());
        assertEquals(RentalInfo.RentalType.HOUSE, rentalInfo.getRentalType());
    }

    @Test
    void reviewRentalShouldRequireReasonWhenRejected() {
        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(1L);
        rentalInfo.setStatus(RentalInfo.RentalStatus.PENDING);
        when(rentalInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(rentalInfo));

        assertThrows(IllegalArgumentException.class, () -> rentalService.reviewRental(1L, 2L, false, ""));
    }

    @Test
    void reviewRentalShouldPersistReviewRecord() {
        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(1L);
        rentalInfo.setStatus(RentalInfo.RentalStatus.PENDING);
        when(rentalInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(rentalInfo));
        when(rentalInfoRepository.save(any(RentalInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rentalService.reviewRental(1L, 2L, true, "");

        ArgumentCaptor<ReviewRecord> captor = ArgumentCaptor.forClass(ReviewRecord.class);
        verify(reviewRecordRepository).save(captor.capture());
        assertEquals("APPROVE", captor.getValue().getAction());
        assertEquals("APPROVED", captor.getValue().getToStatus());
    }

    @Test
    void findPublicRentalByIdShouldRejectNonApprovedRental() {
        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(1L);
        rentalInfo.setStatus(RentalInfo.RentalStatus.PENDING);
        when(rentalInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(rentalInfo));

        assertThrows(com.yxtech.smartlife.exception.NotFoundException.class,
                () -> rentalService.findPublicRentalById(1L));
    }

    @Test
    void findUserRentalByIdShouldReturnOwnedRental() {
        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(1L);
        rentalInfo.setPublisherUserId(2L);
        when(rentalInfoRepository.findByIdAndPublisherUserIdAndDeletedFalse(1L, 2L)).thenReturn(Optional.of(rentalInfo));

        RentalInfo result = rentalService.findUserRentalById(2L, 1L);

        assertEquals(1L, result.getId());
        assertEquals(2L, result.getPublisherUserId());
    }

    @Test
    void offlineRentalShouldPersistOfflineReviewRecord() {
        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(1L);
        rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
        when(rentalInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(rentalInfo));
        when(rentalInfoRepository.save(any(RentalInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rentalService.offlineRental(1L, 2L, "违规信息");

        ArgumentCaptor<ReviewRecord> captor = ArgumentCaptor.forClass(ReviewRecord.class);
        verify(reviewRecordRepository).save(captor.capture());
        assertEquals("OFFLINE", captor.getValue().getAction());
        assertEquals("OFFLINE", captor.getValue().getToStatus());
    }
}
