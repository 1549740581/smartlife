package com.yxtech.smartlife.service;

import com.yxtech.smartlife.dto.AdminLoginResponse;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.repository.AdminRepository;
import com.yxtech.smartlife.repository.RentalInfoRepository;
import com.yxtech.smartlife.repository.ReviewRecordRepository;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.support.AbstractContainerIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AdminAuthServiceContainerTest extends AbstractContainerIntegrationTest {

    private static final String SESSION_KEY_PREFIX = "smart-life:test:admin:session:";

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ReviewRecordRepository reviewRecordRepository;

    @Autowired
    private RentalInfoRepository rentalInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        reviewRecordRepository.deleteAllInBatch();
        rentalInfoRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        adminRepository.deleteAllInBatch();
        flushRedis();
        adminAuthService.ensureDefaultAdmin();
    }

    @Test
    void loginShouldPersistSessionIntoRedis() {
        Admin admin = adminRepository.findByUsernameAndDeletedFalse("admin").orElseThrow();

        AdminLoginResponse response = adminAuthService.login("admin", "admin123");
        String storedAdminId = stringRedisTemplate.opsForValue().get(SESSION_KEY_PREFIX + response.getAdminToken());

        assertEquals(String.valueOf(admin.getId()), storedAdminId);
    }

    @Test
    void requireAdminShouldLoadAdminFromRedisSession() {
        AdminLoginResponse response = adminAuthService.login("admin", "admin123");

        Admin loaded = adminAuthService.requireAdmin(response.getAdminToken());

        assertEquals("admin", loaded.getUsername());
        assertEquals("系统管理员", loaded.getDisplayName());
    }

    @Test
    void logoutShouldDeleteRedisSession() {
        AdminLoginResponse response = adminAuthService.login("admin", "admin123");

        adminAuthService.logout(response.getAdminToken());

        assertNull(stringRedisTemplate.opsForValue().get(SESSION_KEY_PREFIX + response.getAdminToken()));
    }

    private void flushRedis() {
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
}
