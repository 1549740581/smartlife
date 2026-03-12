package com.yxtech.smartlife.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.ReviewRecord;
import com.yxtech.smartlife.exception.NotFoundException;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.ReviewRecordRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.AddressService;
import com.yxtech.smartlife.service.RentalService;
import com.yxtech.smartlife.service.command.CreateRentalCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalInfoRepository rentalInfoRepository;
    private final ReviewRecordRepository reviewRecordRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RentalInfo createRental(CreateRentalCommand command) {
        userRepository.findByIdAndDeletedFalse(command.getPublisherUserId())
                .orElseThrow(() -> new NotFoundException("publisher user not found"));

        String city = normalizeCity(command.getCity());
        String district = trimToNull(command.getDistrict());
        String street = trimToNull(command.getStreet());
        String communityName = trimToNull(command.getCommunityName());
        if (!addressService.exists(city, district, street, communityName)) {
            throw new IllegalArgumentException("selected address is not supported");
        }

        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setPublisherUserId(command.getPublisherUserId());
        rentalInfo.setRentalType(command.getRentalType());
        rentalInfo.setTitle(trimToNull(command.getTitle()));
        rentalInfo.setDescription(trimToNull(command.getDescription()));
        rentalInfo.setPrice(command.getPrice());
        rentalInfo.setContactName(trimToNull(command.getContactName()));
        rentalInfo.setContactPhone(trimToNull(command.getContactPhone()));
        rentalInfo.setCity(city);
        rentalInfo.setDistrict(district);
        rentalInfo.setStreet(street);
        rentalInfo.setCommunityName(communityName);
        rentalInfo.setImageUrls(toJson(command.getImageUrls()));
        rentalInfo.setStatus(RentalInfo.RentalStatus.PENDING);
        return rentalInfoRepository.save(rentalInfo);
    }

    @Override
    public List<RentalInfo> findPublicRentals() {
        return searchPublicRentals(null, null, null, null, null, null);
    }

    @Override
    public List<RentalInfo> searchPublicRentals(
            String keyword,
            RentalInfo.RentalType rentalType,
            String city,
            String district,
            String street,
            String communityName
    ) {
        return rentalInfoRepository.searchPublicRentals(
                RentalInfo.RentalStatus.APPROVED,
                trimToNull(keyword),
                rentalType,
                trimToNull(city),
                trimToNull(district),
                trimToNull(street),
                trimToNull(communityName)
        );
    }

    @Override
    public RentalInfo findPublicRentalById(Long rentalId) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));
        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.APPROVED) {
            throw new NotFoundException("rental not found");
        }
        return rentalInfo;
    }

    @Override
    public List<RentalInfo> findPublicRentalsByType(RentalInfo.RentalType rentalType) {
        return searchPublicRentals(null, rentalType, null, null, null, null);
    }

    @Override
    public List<RentalInfo> findUserRentals(Long userId) {
        return rentalInfoRepository.findByPublisherUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public RentalInfo findUserRentalById(Long userId, Long rentalId) {
        return rentalInfoRepository.findByIdAndPublisherUserIdAndDeletedFalse(rentalId, userId)
                .orElseThrow(() -> new NotFoundException("rental not found"));
    }

    @Override
    public List<RentalInfo> findAllRentals() {
        return rentalInfoRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    public RentalInfo findRentalById(Long rentalId) {
        return rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));
    }

    @Override
    public List<RentalInfo> findPendingRentals() {
        return rentalInfoRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(RentalInfo.RentalStatus.PENDING);
    }

    @Override
    @Transactional
    public RentalInfo reviewRental(Long rentalId, Long adminId, boolean approved, String reason) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));

        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.PENDING) {
            throw new IllegalArgumentException("only pending rental can be reviewed");
        }
        if (!approved && !StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("reject reason must not be blank");
        }

        RentalInfo.RentalStatus fromStatus = rentalInfo.getStatus();
        rentalInfo.setReviewedBy(adminId);
        rentalInfo.setReviewedAt(LocalDateTime.now());
        if (approved) {
            rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
            rentalInfo.setRejectReason(null);
        } else {
            rentalInfo.setStatus(RentalInfo.RentalStatus.REJECTED);
            rentalInfo.setRejectReason(reason);
        }
        RentalInfo saved = rentalInfoRepository.save(rentalInfo);

        saveReviewRecord(saved.getId(), approved ? "APPROVE" : "REJECT", fromStatus, saved.getStatus(), reason, adminId);
        return saved;
    }

    @Override
    @Transactional
    public RentalInfo offlineRental(Long rentalId, Long adminId, String reason) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(rentalId)
                .orElseThrow(() -> new NotFoundException("rental not found"));

        if (rentalInfo.getStatus() != RentalInfo.RentalStatus.APPROVED) {
            throw new IllegalArgumentException("only approved rental can be offline");
        }

        RentalInfo.RentalStatus fromStatus = rentalInfo.getStatus();
        rentalInfo.setStatus(RentalInfo.RentalStatus.OFFLINE);
        rentalInfo.setReviewedBy(adminId);
        rentalInfo.setReviewedAt(LocalDateTime.now());
        rentalInfo.setRejectReason(reason);
        RentalInfo saved = rentalInfoRepository.save(rentalInfo);

        saveReviewRecord(saved.getId(), "OFFLINE", fromStatus, saved.getStatus(), reason, adminId);
        return saved;
    }

    private void saveReviewRecord(
            Long rentalId,
            String action,
            RentalInfo.RentalStatus fromStatus,
            RentalInfo.RentalStatus toStatus,
            String reason,
            Long adminId
    ) {
        ReviewRecord reviewRecord = new ReviewRecord();
        reviewRecord.setRentalInfoId(rentalId);
        reviewRecord.setAction(action);
        reviewRecord.setFromStatus(fromStatus.name());
        reviewRecord.setToStatus(toStatus.name());
        reviewRecord.setReason(reason);
        reviewRecord.setOperatorId(adminId);
        reviewRecordRepository.save(reviewRecord);
    }

    private String toJson(List<String> imageUrls) {
        List<String> safeList = imageUrls == null ? Collections.emptyList() : imageUrls;
        try {
            return objectMapper.writeValueAsString(safeList);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("image urls serialization failed");
        }
    }

    private String normalizeCity(String city) {
        String normalized = trimToNull(city);
        if (!StringUtils.hasText(normalized)) {
            return "杭州";
        }
        if (!"杭州".equals(normalized)) {
            throw new IllegalArgumentException("currently only Hangzhou is supported");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
