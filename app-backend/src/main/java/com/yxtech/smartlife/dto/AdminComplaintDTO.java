package com.yxtech.smartlife.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.entity.Complaint;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.service.model.ComplaintAggregate;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class AdminComplaintDTO {

    private Long id;
    private Long complainantUserId;
    private String complainantNickname;
    private Long rentalInfoId;
    private String rentalTitle;
    private String rentalType;
    private Long targetUserId;
    private String targetUserNickname;
    private String targetUserStatus;
    private Integer targetUserWarningCount;
    private String reason;
    private List<String> evidenceUrls;
    private String status;
    private Long processedBy;
    private LocalDateTime processedAt;
    private String processRemark;
    private LocalDateTime createdAt;

    public static AdminComplaintDTO fromAggregate(ComplaintAggregate aggregate, ObjectMapper objectMapper) {
        Complaint complaint = aggregate.complaint();
        User complainant = aggregate.complainant();
        User targetUser = aggregate.targetUser();
        RentalInfo rentalInfo = aggregate.rentalInfo();

        AdminComplaintDTO dto = new AdminComplaintDTO();
        dto.setId(complaint.getId());
        dto.setComplainantUserId(complaint.getComplainantUserId());
        dto.setComplainantNickname(complainant != null ? complainant.getNickname() : null);
        dto.setRentalInfoId(complaint.getRentalInfoId());
        dto.setRentalTitle(rentalInfo != null ? rentalInfo.getTitle() : null);
        dto.setRentalType(rentalInfo != null ? rentalInfo.getRentalType().name() : null);
        dto.setTargetUserId(complaint.getTargetUserId());
        dto.setTargetUserNickname(targetUser != null ? targetUser.getNickname() : null);
        dto.setTargetUserStatus(targetUser != null ? targetUser.getStatus().name() : null);
        dto.setTargetUserWarningCount(targetUser != null ? targetUser.getWarningCount() : null);
        dto.setReason(complaint.getReason());
        dto.setEvidenceUrls(parseJson(complaint.getEvidenceUrls(), objectMapper));
        dto.setStatus(complaint.getStatus().name());
        dto.setProcessedBy(complaint.getProcessedBy());
        dto.setProcessedAt(complaint.getProcessedAt());
        dto.setProcessRemark(complaint.getProcessRemark());
        dto.setCreatedAt(complaint.getCreatedAt());
        return dto;
    }

    private static List<String> parseJson(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
