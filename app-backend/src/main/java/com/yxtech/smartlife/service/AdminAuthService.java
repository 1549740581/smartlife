package com.yxtech.smartlife.service;

import com.yxtech.smartlife.config.AdminAuthProperties;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.exception.NotFoundException;
import com.yxtech.smartlife.exception.UnauthorizedException;
import com.yxtech.smartlife.repository.AdminRepository;
import com.yxtech.smartlife.dto.AdminLoginResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;

@Service
@DependsOnDatabaseInitialization
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock adminAuthClock;
    private final AdminAuthProperties adminAuthProperties;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public AdminAuthService(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            @Qualifier("adminAuthClock") Clock adminAuthClock,
            AdminAuthProperties adminAuthProperties,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminAuthClock = adminAuthClock;
        this.adminAuthProperties = adminAuthProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void ensureDefaultAdmin() {
        if (adminRepository.count() > 0) {
            return;
        }
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setDisplayName("系统管理员");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setStatus(Admin.AdminStatus.ACTIVE);
        adminRepository.save(admin);
    }

    public AdminLoginResponse login(String username, String password) {
        Admin admin = adminRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UnauthorizedException("invalid admin credentials"));
        if (admin.getStatus() != Admin.AdminStatus.ACTIVE) {
            throw new UnauthorizedException("admin disabled");
        }
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new UnauthorizedException("invalid admin credentials");
        }

        String token = UUID.randomUUID().toString();
        Instant expiresAt = buildExpiresAt(adminAuthClock.instant());
        sessionValueOperations().set(buildSessionKey(token), String.valueOf(admin.getId()), sessionTtl());
        return AdminLoginResponse.builder()
                .adminId(admin.getId())
                .displayName(admin.getDisplayName())
                .adminToken(token)
                .expiresAt(LocalDateTime.ofInstant(expiresAt, adminAuthClock.getZone()))
                .build();
    }

    public Admin requireAdmin(String token) {
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("admin token invalid");
        }
        String adminIdValue = sessionValueOperations().get(buildSessionKey(token));
        if (!StringUtils.hasText(adminIdValue)) {
            throw new UnauthorizedException("admin token invalid");
        }
        Instant now = adminAuthClock.instant();
        Instant expiresAt = buildExpiresAt(now);
        Boolean expiryUpdated = stringRedisTemplate.expire(buildSessionKey(token), sessionTtl());
        if (Boolean.FALSE.equals(expiryUpdated)) {
            throw new UnauthorizedException("admin token expired");
        }
        Long adminId = parseAdminId(adminIdValue);
        return adminRepository.findById(adminId).orElseThrow(() -> new NotFoundException("admin not found"));
    }

    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            stringRedisTemplate.delete(buildSessionKey(token));
        }
    }

    private Instant buildExpiresAt(Instant from) {
        return from.plusSeconds(adminAuthProperties.getSessionTtlMinutes() * 60);
    }

    private Duration sessionTtl() {
        return Duration.ofMinutes(adminAuthProperties.getSessionTtlMinutes());
    }

    private String buildSessionKey(String token) {
        return adminAuthProperties.getSessionKeyPrefix() + token;
    }

    private ValueOperations<String, String> sessionValueOperations() {
        return stringRedisTemplate.opsForValue();
    }

    private Long parseAdminId(String adminIdValue) {
        try {
            return Long.valueOf(adminIdValue);
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("admin token invalid");
        }
    }
}
