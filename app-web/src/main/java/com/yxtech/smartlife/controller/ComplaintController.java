package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.ComplaintDTO;
import com.yxtech.smartlife.dto.ComplaintDetailRequest;
import com.yxtech.smartlife.dto.CreateComplaintRequest;
import com.yxtech.smartlife.dto.UserComplaintsRequest;
import com.yxtech.smartlife.entity.Complaint;
import com.yxtech.smartlife.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Result<ComplaintDTO>> createComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        Complaint complaint = complaintService.createComplaint(
                request.getComplainantUserId(),
                request.getRentalInfoId(),
                request.getReason(),
                request.getEvidenceUrls()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(ComplaintDTO.fromAggregate(
                        complaintService.findComplaintById(complaint.getId()),
                        objectMapper
                )));
    }

    @PostMapping("/user")
    public Result<List<ComplaintDTO>> getUserComplaints(@Valid @RequestBody UserComplaintsRequest request) {
        return Result.success(complaintService.findUserComplaints(request.getUserId()).stream()
                .map(aggregate -> ComplaintDTO.fromAggregate(aggregate, objectMapper))
                .toList());
    }

    @PostMapping("/detail")
    public Result<ComplaintDTO> getComplaintDetail(@Valid @RequestBody ComplaintDetailRequest request) {
        return Result.success(ComplaintDTO.fromAggregate(
                complaintService.findComplaintById(request.getId()),
                objectMapper
        ));
    }
}
