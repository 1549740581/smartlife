package com.yxtech.smartlife.controller.adminweb;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.adminweb.IdRequest;
import com.yxtech.smartlife.dto.adminweb.OfflineRequest;
import com.yxtech.smartlife.dto.adminweb.AdminPageRequest;
import com.yxtech.smartlife.dto.adminweb.ReviewRequest;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.service.RentalService;
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
@RequestMapping("/api/admin-web/rentals")
@RequiredArgsConstructor
public class AdminWebRentalController {

    private final RentalInfoRepository rentalInfoRepository;
    private final RentalService rentalService;

    @PostMapping("/list")
    public Result<Map<String, Object>> list(@RequestBody AdminPageRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        String status = request.getStatus();
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());

        Page<RentalInfo> rentals;
        if (status != null && !status.isEmpty()) {
            RentalInfo.RentalStatus rentalStatus = RentalInfo.RentalStatus.valueOf(status);
            rentals = rentalInfoRepository.findByStatusAndDeletedFalse(rentalStatus, pageable);
        } else {
            rentals = rentalInfoRepository.findByDeletedFalse(pageable);
        }

        List<Map<String, Object>> list = rentals.getContent().stream().map(this::toMap).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", rentals.getTotalElements());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @PostMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestBody IdRequest request) {
        Long id = request.getId();
        RentalInfo rental = rentalInfoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("rental not found"));

        Map<String, Object> result = toMap(rental);
        return Result.success(result);
    }

    @PostMapping("/review")
    public Result<Void> review(@RequestBody ReviewRequest request) {
        Long id = request.getId();
        boolean approved = Boolean.TRUE.equals(request.getApproved());
        String reason = request.getReason();
        // TODO: 从认证上下文获取 adminId
        Long adminId = 1L;
        rentalService.reviewRental(id, adminId, approved, reason);
        return Result.success();
    }

    @PostMapping("/offline")
    public Result<Void> offline(@RequestBody OfflineRequest request) {
        Long id = request.getId();
        String reason = request.getReason() != null ? request.getReason() : "管理员下架";
        // TODO: 从认证上下文获取 adminId
        Long adminId = 1L;
        rentalService.offlineRental(id, adminId, reason);
        return Result.success();
    }

    private Map<String, Object> toMap(RentalInfo rental) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", rental.getId());
        map.put("title", rental.getTitle());
        map.put("rentalType", rental.getRentalType().name());
        map.put("status", rental.getStatus().name());
        map.put("price", rental.getPrice());
        map.put("priceUnit", "元/月");
        map.put("city", rental.getCity());
        map.put("district", rental.getDistrict());
        map.put("street", rental.getStreet());
        map.put("communityName", rental.getCommunityName());
        map.put("description", rental.getDescription());
        map.put("imageUrls", rental.getImageUrls());
        map.put("publisherUserId", rental.getPublisherUserId());
        map.put("createdAt", rental.getCreatedAt() != null ? rental.getCreatedAt().toString() : null);
        return map;
    }
}
