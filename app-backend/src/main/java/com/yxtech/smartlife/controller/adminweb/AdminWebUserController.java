package com.yxtech.smartlife.controller.adminweb;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.adminweb.AdminPageRequest;
import com.yxtech.smartlife.dto.adminweb.IdRequest;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.UserRepository;
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
@RequestMapping("/api/admin-web/users")
@RequiredArgsConstructor
public class AdminWebUserController {

    private final UserRepository userRepository;
    private final RentalInfoRepository rentalInfoRepository;

    @PostMapping("/list")
    public Result<Map<String, Object>> list(@RequestBody AdminPageRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        String status = request.getStatus();
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());

        Page<User> users;
        if (status != null && !status.isEmpty()) {
            User.UserStatus userStatus = User.UserStatus.valueOf(status);
            users = userRepository.findByStatus(userStatus, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        List<Map<String, Object>> list = users.getContent().stream().map(this::toListMap).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", users.getTotalElements());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @PostMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestBody IdRequest request) {
        Long id = request.getId();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Map<String, Object> result = toDetailMap(user);
        return Result.success(result);
    }

    @PostMapping("/rentals")
    public Result<List<Map<String, Object>>> userRentals(@RequestBody IdRequest request) {
        Long id = request.getId();
        List<RentalInfo> rentals = rentalInfoRepository.findByPublisherUserIdAndDeletedFalseOrderByCreatedAtDesc(id);

        List<Map<String, Object>> list = rentals.stream().map(rental -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rental.getId());
            map.put("title", rental.getTitle());
            map.put("rentalType", rental.getRentalType().name());
            map.put("status", rental.getStatus().name());
            map.put("createdAt", rental.getCreatedAt() != null ? rental.getCreatedAt().toString() : null);
            return map;
        }).collect(Collectors.toList());

        return Result.success(list);
    }

    @PostMapping("/lock")
    public Result<Void> lock(@RequestBody IdRequest request) {
        Long id = request.getId();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        user.setStatus(User.UserStatus.LOCKED);
        userRepository.save(user);

        rentalInfoRepository.findByPublisherUserIdAndDeletedFalseOrderByCreatedAtDesc(id).forEach(rental -> {
            if (rental.getStatus() == RentalInfo.RentalStatus.APPROVED) {
                rental.setStatus(RentalInfo.RentalStatus.OFFLINE);
                rentalInfoRepository.save(rental);
            }
        });

        return Result.success();
    }

    @PostMapping("/unlock")
    public Result<Void> unlock(@RequestBody IdRequest request) {
        Long id = request.getId();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        user.setStatus(User.UserStatus.ACTIVE);
        user.setWarningCount(0);
        userRepository.save(user);

        return Result.success();
    }

    private Map<String, Object> toListMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("nickname", user.getNickname());
        map.put("phone", user.getPhone());
        map.put("status", user.getStatus().name());
        map.put("warningCount", user.getWarningCount());
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

        long rentalCount = rentalInfoRepository.findByPublisherUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getId()).size();
        map.put("rentalCount", rentalCount);

        return map;
    }

    private Map<String, Object> toDetailMap(User user) {
        Map<String, Object> map = toListMap(user);
        map.put("email", user.getEmail());
        return map;
    }
}
