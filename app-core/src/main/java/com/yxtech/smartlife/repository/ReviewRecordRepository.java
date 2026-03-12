package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {

    List<ReviewRecord> findByRentalInfoIdOrderByCreatedAtDesc(Long rentalInfoId);
}
