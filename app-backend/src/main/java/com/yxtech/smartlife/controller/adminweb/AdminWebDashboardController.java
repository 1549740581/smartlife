package com.yxtech.smartlife.controller.adminweb;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.repository.ComplaintRepository;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin-web/dashboard")
@RequiredArgsConstructor
public class AdminWebDashboardController {

    private final RentalInfoRepository rentalInfoRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;

    @PostMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> result = new HashMap<>();

        long totalRentals = rentalInfoRepository.count();
        long pendingRentals = rentalInfoRepository.countByStatusAndDeletedFalse(RentalInfo.RentalStatus.PENDING);
        long totalUsers = userRepository.count();
        long pendingComplaints = complaintRepository.countByStatus(com.yxtech.smartlife.entity.Complaint.ComplaintStatus.PENDING);

        result.put("totalRentals", totalRentals);
        result.put("pendingRentals", pendingRentals);
        result.put("totalUsers", totalUsers);
        result.put("pendingComplaints", pendingComplaints);

        return Result.success(result);
    }

    @PostMapping("/trends")
    public Result<Map<String, Object>> getTrends() {
        Map<String, Object> result = new HashMap<>();

        List<String> dates = new ArrayList<>();
        List<Long> newRentals = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.toString());

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            long rentalCount = rentalInfoRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            long userCount = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);

            newRentals.add(rentalCount);
            newUsers.add(userCount);
        }

        result.put("dates", dates);
        result.put("newRentals", newRentals);
        result.put("newUsers", newUsers);

        return Result.success(result);
    }

    @PostMapping("/distributions")
    public Result<Map<String, Object>> getDistributions() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> typeDistribution = new ArrayList<>();
        for (RentalInfo.RentalType type : RentalInfo.RentalType.values()) {
            long count = rentalInfoRepository.countByRentalTypeAndDeletedFalse(type);
            Map<String, Object> item = new HashMap<>();
            item.put("name", type.name());
            item.put("value", count);
            typeDistribution.add(item);
        }

        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        for (RentalInfo.RentalStatus status : RentalInfo.RentalStatus.values()) {
            long count = rentalInfoRepository.countByStatusAndDeletedFalse(status);
            Map<String, Object> item = new HashMap<>();
            item.put("name", status.name());
            item.put("value", count);
            statusDistribution.add(item);
        }

        result.put("typeDistribution", typeDistribution);
        result.put("statusDistribution", statusDistribution);

        return Result.success(result);
    }
}
