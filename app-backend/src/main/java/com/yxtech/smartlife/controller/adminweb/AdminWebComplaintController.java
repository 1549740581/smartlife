package com.yxtech.smartlife.controller.adminweb;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.adminweb.AdminPageRequest;
import com.yxtech.smartlife.dto.adminweb.IdRequest;
import com.yxtech.smartlife.dto.adminweb.ProcessComplaintRequest;
import com.yxtech.smartlife.entity.Complaint;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.repository.ComplaintRepository;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin-web/complaints")
@RequiredArgsConstructor
public class AdminWebComplaintController {

    private final ComplaintRepository complaintRepository;
    private final RentalInfoRepository rentalInfoRepository;
    private final UserRepository userRepository;
    private final ComplaintService complaintService;

    @PostMapping("/list")
    public Result<Map<String, Object>> list(@RequestBody AdminPageRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        String status = request.getStatus();
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());

        Page<Complaint> complaints;
        if (status != null && !status.isEmpty()) {
            Complaint.ComplaintStatus complaintStatus = Complaint.ComplaintStatus.valueOf(status);
            complaints = complaintRepository.findByStatus(complaintStatus, pageable);
        } else {
            complaints = complaintRepository.findAll(pageable);
        }

        List<Map<String, Object>> list = complaints.getContent().stream().map(this::toListMap).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", complaints.getTotalElements());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @PostMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestBody IdRequest request) {
        Long id = request.getId();
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("complaint not found"));

        Map<String, Object> result = toDetailMap(complaint);
        return Result.success(result);
    }

    @PostMapping("/process")
    public Result<Void> process(@RequestBody ProcessComplaintRequest request) {
        Long id = request.getId();
        boolean accepted = Boolean.TRUE.equals(request.getAccepted());
        String resultText = request.getResult();
        // TODO: 从认证上下文获取 adminId
        Long adminId = 1L;
        complaintService.processComplaint(id, adminId, accepted, resultText);
        return Result.success();
    }

    private Map<String, Object> toListMap(Complaint complaint) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", complaint.getId());
        map.put("rentalId", complaint.getRentalInfoId());
        map.put("reason", complaint.getReason());
        map.put("status", complaint.getStatus().name());
        map.put("createdAt", complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString() : null);

        RentalInfo rental = rentalInfoRepository.findById(complaint.getRentalInfoId()).orElse(null);
        map.put("rentalTitle", rental != null ? rental.getTitle() : "已删除");

        User complainant = userRepository.findById(complaint.getComplainantUserId()).orElse(null);
        map.put("complainantNickname", complainant != null ? complainant.getNickname() : "未知用户");
        map.put("complainantUserId", complaint.getComplainantUserId());

        User respondent = userRepository.findById(complaint.getTargetUserId()).orElse(null);
        map.put("respondentNickname", respondent != null ? respondent.getNickname() : "未知用户");
        map.put("respondentUserId", complaint.getTargetUserId());

        return map;
    }

    private Map<String, Object> toDetailMap(Complaint complaint) {
        Map<String, Object> map = toListMap(complaint);

        map.put("evidenceUrls", complaint.getEvidenceUrls());
        map.put("processResult", complaint.getProcessRemark());
        map.put("processedAt", complaint.getProcessedAt() != null ? complaint.getProcessedAt().toString() : null);

        User respondentDetail = userRepository.findById(complaint.getTargetUserId()).orElse(null);
        map.put("respondentWarningCount", respondentDetail != null ? respondentDetail.getWarningCount() : 0);

        return map;
    }
}
