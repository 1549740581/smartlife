package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByIdAndDeletedFalse(Long id);

    List<Complaint> findByComplainantUserIdAndDeletedFalseOrderByCreatedAtDesc(Long complainantUserId);

    List<Complaint> findByTargetUserIdAndDeletedFalseOrderByCreatedAtDesc(Long targetUserId);

    List<Complaint> findByStatusAndDeletedFalseOrderByCreatedAtDesc(Complaint.ComplaintStatus status);

    List<Complaint> findByDeletedFalseOrderByCreatedAtDesc();

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.targetUserId = :targetUserId AND c.status = 'ACCEPTED' AND c.deleted = false")
    long countAcceptedComplaintsByTargetUserId(@Param("targetUserId") Long targetUserId);

    boolean existsByComplainantUserIdAndRentalInfoIdAndDeletedFalse(Long complainantUserId, Long rentalInfoId);
}
