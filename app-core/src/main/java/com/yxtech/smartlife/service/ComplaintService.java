package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.Complaint;
import com.yxtech.smartlife.service.model.ComplaintAggregate;

import java.util.List;

public interface ComplaintService {

    Complaint createComplaint(Long complainantUserId, Long rentalInfoId, String reason, List<String> evidenceUrls);

    List<ComplaintAggregate> findUserComplaints(Long userId);

    List<ComplaintAggregate> findPendingComplaints();

    List<ComplaintAggregate> findAllComplaints();

    ComplaintAggregate findComplaintById(Long id);

    Complaint processComplaint(Long complaintId, Long adminId, boolean accepted, String remark);

    void unlockUser(Long userId, Long adminId);
}
