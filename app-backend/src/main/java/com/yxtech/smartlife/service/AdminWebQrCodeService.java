package com.yxtech.smartlife.service;

import com.yxtech.smartlife.config.AdminAuthProperties;
import com.yxtech.smartlife.dto.AdminLoginResponse;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminWebQrCodeService {

    private static final String QR_CODE_PREFIX = "admin:qrcode:";
    private static final Duration QR_CODE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final AdminRepository adminRepository;
    private final AdminAuthProperties adminAuthProperties;
    private final Clock adminAuthClock;

    @Autowired
    public AdminWebQrCodeService(
            StringRedisTemplate stringRedisTemplate,
            AdminRepository adminRepository,
            AdminAuthProperties adminAuthProperties,
            @Qualifier("adminAuthClock") Clock adminAuthClock
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.adminRepository = adminRepository;
        this.adminAuthProperties = adminAuthProperties;
        this.adminAuthClock = adminAuthClock;
    }

    public Map<String, Object> generateQrCode() {
        String ticket = UUID.randomUUID().toString();
        String qrCodeUrl = "https://api.example.com/wechat/qrcode?ticket=" + ticket;

        stringRedisTemplate.opsForValue().set(
                QR_CODE_PREFIX + ticket,
                "PENDING",
                QR_CODE_TTL
        );

        Map<String, Object> result = new HashMap<>();
        result.put("ticket", ticket);
        result.put("qrCodeUrl", qrCodeUrl);
        result.put("expireSeconds", QR_CODE_TTL.getSeconds());
        return result;
    }

    public Map<String, Object> checkStatus(String ticket) {
        String status = stringRedisTemplate.opsForValue().get(QR_CODE_PREFIX + ticket);
        Map<String, Object> result = new HashMap<>();

        if (status == null) {
            result.put("status", "EXPIRED");
            return result;
        }

        if (status.equals("PENDING")) {
            result.put("status", "PENDING");
            return result;
        }

        if (status.startsWith("SCANNED:")) {
            result.put("status", "SCANNED");
            return result;
        }

        if (status.startsWith("CONFIRMED:")) {
            Long adminId = Long.parseLong(status.substring("CONFIRMED:".length()));
            Admin admin = adminRepository.findById(adminId).orElse(null);
            if (admin == null) {
                result.put("status", "ERROR");
                return result;
            }

            stringRedisTemplate.delete(QR_CODE_PREFIX + ticket);

            String token = UUID.randomUUID().toString();
            Instant expiresAt = adminAuthClock.instant().plusSeconds(adminAuthProperties.getSessionTtlMinutes() * 60L);
            stringRedisTemplate.opsForValue().set(
                    adminAuthProperties.getSessionKeyPrefix() + token,
                    String.valueOf(admin.getId()),
                    Duration.ofMinutes(adminAuthProperties.getSessionTtlMinutes())
            );

            AdminLoginResponse loginResponse = AdminLoginResponse.builder()
                    .adminId(admin.getId())
                    .displayName(admin.getDisplayName())
                    .adminToken(token)
                    .expiresAt(LocalDateTime.ofInstant(expiresAt, adminAuthClock.getZone()))
                    .build();

            result.put("status", "CONFIRMED");
            result.put("token", loginResponse.getAdminToken());
            result.put("adminId", loginResponse.getAdminId());
            result.put("displayName", loginResponse.getDisplayName());
            result.put("expiresAt", loginResponse.getExpiresAt().toString());
            return result;
        }

        result.put("status", "PENDING");
        return result;
    }

    public void markScanned(String ticket, String openId) {
        String key = QR_CODE_PREFIX + ticket;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.opsForValue().set(key, "SCANNED:" + openId, QR_CODE_TTL);
        }
    }

    public void confirmLogin(String ticket, Long adminId) {
        String key = QR_CODE_PREFIX + ticket;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.opsForValue().set(key, "CONFIRMED:" + adminId, QR_CODE_TTL);
        }
    }
}
