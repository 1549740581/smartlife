package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.auth.CurrentAdmin;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.AdminComplaintDTO;
import com.yxtech.smartlife.dto.AdminComplaintDetailRequest;
import com.yxtech.smartlife.dto.ProcessComplaintRequest;
import com.yxtech.smartlife.dto.UnlockUserRequest;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.entity.Complaint;
import com.yxtech.smartlife.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final ComplaintService complaintService;
    private final ObjectMapper objectMapper;

    @PostMapping("/pending")
    public Result<List<AdminComplaintDTO>> getPendingComplaints() {
        return Result.success(complaintService.findPendingComplaints().stream()
                .map(aggregate -> AdminComplaintDTO.fromAggregate(aggregate, objectMapper))
                .toList());
    }

    @PostMapping("/list")
    public Result<List<AdminComplaintDTO>> getAllComplaints() {
        return Result.success(complaintService.findAllComplaints().stream()
                .map(aggregate -> AdminComplaintDTO.fromAggregate(aggregate, objectMapper))
                .toList());
    }

    @PostMapping("/detail")
    public Result<AdminComplaintDTO> getComplaintDetail(@Valid @RequestBody AdminComplaintDetailRequest request) {
        return Result.success(AdminComplaintDTO.fromAggregate(
                complaintService.findComplaintById(request.getId()),
                objectMapper
        ));
    }

    @PostMapping("/process")
    public Result<AdminComplaintDTO> processComplaint(
            @CurrentAdmin Admin admin,
            @Valid @RequestBody ProcessComplaintRequest request
    ) {
        Complaint complaint = complaintService.processComplaint(
                request.getId(),
                admin.getId(),
                request.getAccepted(),
                request.getRemark()
        );
        return Result.success(AdminComplaintDTO.fromAggregate(
                complaintService.findComplaintById(complaint.getId()),
                objectMapper
        ));
    }

    @PostMapping("/unlock")
    public Result<Void> unlockUser(
            @CurrentAdmin Admin admin,
            @Valid @RequestBody UnlockUserRequest request
    ) {
        complaintService.unlockUser(request.getUserId(), admin.getId());
        return Result.success();
    }
}
