package com.yxtech.smartlife.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.Complaint;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.exception.NotFoundException;
import com.yxtech.smartlife.repository.ComplaintRepository;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.ComplaintService;
import com.yxtech.smartlife.service.RentalService;
import com.yxtech.smartlife.service.model.ComplaintAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private static final int WARNING_THRESHOLD = 2;

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final RentalInfoRepository rentalInfoRepository;
    private final RentalService rentalService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Complaint createComplaint(Long complainantUserId, Long rentalInfoId, String reason, List<String> evidenceUrls) {
        User complainant = userRepository.findByIdAndDeletedFalse(complainantUserId)
                .orElseThrow(() -> new NotFoundException("complainant user not found"));

        if (complainant.getStatus() == User.UserStatus.LOCKED) {
            throw new IllegalArgumentException("locked user cannot create complaint");
        }

        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(rentalInfoId)
                .orElseThrow(() -> new NotFoundException("rental info not found"));

        if (rentalInfo.getPublisherUserId().equals(complainantUserId)) {
            throw new IllegalArgumentException("cannot complain about your own rental");
        }

        if (complaintRepository.existsByComplainantUserIdAndRentalInfoIdAndDeletedFalse(complainantUserId, rentalInfoId)) {
            throw new IllegalArgumentException("you have already complained about this rental");
        }

        Complaint complaint = new Complaint();
        complaint.setComplainantUserId(complainantUserId);
        complaint.setRentalInfoId(rentalInfoId);
        complaint.setTargetUserId(rentalInfo.getPublisherUserId());
        complaint.setReason(reason);
        complaint.setEvidenceUrls(toJson(evidenceUrls));
        complaint.setStatus(Complaint.ComplaintStatus.PENDING);

        log.info("User {} created complaint against rental {} (owner: {})", complainantUserId, rentalInfoId, rentalInfo.getPublisherUserId());
        return complaintRepository.save(complaint);
    }

    @Override
    public List<ComplaintAggregate> findUserComplaints(Long userId) {
        return complaintRepository.findByComplainantUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildAggregate)
                .toList();
    }

    @Override
    public List<ComplaintAggregate> findPendingComplaints() {
        return complaintRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(Complaint.ComplaintStatus.PENDING)
                .stream()
                .map(this::buildAggregate)
                .toList();
    }

    @Override
    public List<ComplaintAggregate> findAllComplaints() {
        return complaintRepository.findByDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::buildAggregate)
                .toList();
    }

    @Override
    public ComplaintAggregate findComplaintById(Long id) {
        Complaint complaint = complaintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("complaint not found"));
        return buildAggregate(complaint);
    }

    @Override
    @Transactional
    public Complaint processComplaint(Long complaintId, Long adminId, boolean accepted, String remark) {
        Complaint complaint = complaintRepository.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new NotFoundException("complaint not found"));

        if (complaint.getStatus() != Complaint.ComplaintStatus.PENDING) {
            throw new IllegalArgumentException("complaint has already been processed");
        }

        complaint.setProcessedBy(adminId);
        complaint.setProcessedAt(LocalDateTime.now());
        complaint.setProcessRemark(remark);

        if (accepted) {
            complaint.setStatus(Complaint.ComplaintStatus.ACCEPTED);
            handleAcceptedComplaint(complaint, adminId);
        } else {
            complaint.setStatus(Complaint.ComplaintStatus.REJECTED);
        }

        log.info("Admin {} processed complaint {}: accepted={}", adminId, complaintId, accepted);
        return complaintRepository.save(complaint);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId, Long adminId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (user.getStatus() != User.UserStatus.LOCKED) {
            throw new IllegalArgumentException("user is not locked");
        }

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Admin {} unlocked user {} (warning count remains: {})", adminId, userId, user.getWarningCount());
    }

    private void handleAcceptedComplaint(Complaint complaint, Long adminId) {
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(complaint.getRentalInfoId())
                .orElse(null);

        if (rentalInfo != null && rentalInfo.getStatus() == RentalInfo.RentalStatus.APPROVED) {
            rentalService.offlineRental(rentalInfo.getId(), adminId, "因投诉核实强制下架");
        }

        User targetUser = userRepository.findByIdAndDeletedFalse(complaint.getTargetUserId())
                .orElseThrow(() -> new NotFoundException("target user not found"));

        int newWarningCount = targetUser.getWarningCount() + 1;
        targetUser.setWarningCount(newWarningCount);

        log.info("User {} warning count increased to {}", targetUser.getId(), newWarningCount);

        if (newWarningCount >= WARNING_THRESHOLD) {
            targetUser.setStatus(User.UserStatus.LOCKED);
            userRepository.save(targetUser);

            offlineAllUserRentals(targetUser.getId(), adminId);

            log.info("User {} locked due to {} warnings", targetUser.getId(), newWarningCount);
        } else {
            userRepository.save(targetUser);
        }
    }

    private void offlineAllUserRentals(Long userId, Long adminId) {
        List<RentalInfo> userRentals = rentalInfoRepository.findByPublisherUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
        for (RentalInfo rental : userRentals) {
            if (rental.getStatus() == RentalInfo.RentalStatus.APPROVED) {
                rentalService.offlineRental(rental.getId(), adminId, "用户账号锁定，信息强制下架");
            }
        }
    }

    private ComplaintAggregate buildAggregate(Complaint complaint) {
        User complainant = userRepository.findByIdAndDeletedFalse(complaint.getComplainantUserId()).orElse(null);
        User targetUser = userRepository.findByIdAndDeletedFalse(complaint.getTargetUserId()).orElse(null);
        RentalInfo rentalInfo = rentalInfoRepository.findByIdAndDeletedFalse(complaint.getRentalInfoId()).orElse(null);
        return new ComplaintAggregate(complaint, complainant, targetUser, rentalInfo);
    }

    private String toJson(List<String> urls) {
        List<String> safeList = urls == null ? Collections.emptyList() : urls;
        try {
            return objectMapper.writeValueAsString(safeList);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("evidence urls serialization failed");
        }
    }
}
