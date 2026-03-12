package com.yxtech.smartlife.service;

import com.yxtech.smartlife.config.AdminAuthProperties;
import com.yxtech.smartlife.dto.AdminLoginResponse;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.exception.UnauthorizedException;
import com.yxtech.smartlife.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAuthServiceTest {

    private final AdminRepository adminRepository = mock(AdminRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-03-12T02:00:00Z"), ZoneId.of("Asia/Shanghai"));
    private final AdminAuthProperties adminAuthProperties = new AdminAuthProperties();
    private final AdminAuthService adminAuthService = new AdminAuthService(
            adminRepository,
            passwordEncoder,
            clock,
            adminAuthProperties,
            stringRedisTemplate
    );

    @Test
    void shouldDependOnDatabaseInitialization() {
        assertTrue(AdminAuthService.class.isAnnotationPresent(DependsOnDatabaseInitialization.class));
    }

    AdminAuthServiceTest() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void ensureDefaultAdminShouldCreateAdminWhenRepositoryIsEmpty() {
        when(adminRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-password");

        adminAuthService.ensureDefaultAdmin();

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());
        assertEquals("admin", captor.getValue().getUsername());
        assertEquals("系统管理员", captor.getValue().getDisplayName());
        assertEquals("encoded-password", captor.getValue().getPasswordHash());
        assertEquals(Admin.AdminStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void ensureDefaultAdminShouldSkipWhenRepositoryAlreadyHasAdmin() {
        when(adminRepository.count()).thenReturn(1L);

        adminAuthService.ensureDefaultAdmin();

        verify(passwordEncoder, never()).encode("admin123");
        verify(adminRepository, never()).save(org.mockito.ArgumentMatchers.any(Admin.class));
    }

    @Test
    void loginShouldReturnSessionTokenForValidAdmin() {
        adminAuthProperties.setSessionTtlMinutes(120);
        adminAuthProperties.setSessionKeyPrefix("smart-life:admin:session:");
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setDisplayName("系统管理员");
        admin.setStatus(Admin.AdminStatus.ACTIVE);
        admin.setPasswordHash("encoded-password");

        when(adminRepository.findByUsernameAndDeletedFalse("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin123", "encoded-password")).thenReturn(true);

        AdminLoginResponse response = adminAuthService.login("admin", "admin123");

        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq("smart-life:admin:session:" + response.getAdminToken()),
                org.mockito.ArgumentMatchers.eq("1"),
                org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(120))
        );
        assertEquals(1L, response.getAdminId());
        assertEquals("系统管理员", response.getDisplayName());
        assertNotNull(response.getAdminToken());
        assertEquals(LocalDateTime.of(2026, 3, 12, 12, 0), response.getExpiresAt());
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setStatus(Admin.AdminStatus.ACTIVE);
        admin.setPasswordHash("encoded-password");

        when(adminRepository.findByUsernameAndDeletedFalse("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> adminAuthService.login("admin", "wrong-password"));
    }

    @Test
    void requireAdminShouldRejectExpiredToken() {
        adminAuthProperties.setSessionTtlMinutes(1);
        adminAuthProperties.setSessionKeyPrefix("smart-life:admin:session:");
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setDisplayName("系统管理员");
        admin.setStatus(Admin.AdminStatus.ACTIVE);
        admin.setPasswordHash("encoded-password");

        when(adminRepository.findByUsernameAndDeletedFalse("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin123", "encoded-password")).thenReturn(true);

        AdminLoginResponse response = adminAuthService.login("admin", "admin123");
        when(valueOperations.get("smart-life:admin:session:" + response.getAdminToken())).thenReturn("1");
        when(stringRedisTemplate.expire("smart-life:admin:session:" + response.getAdminToken(), Duration.ofMinutes(1)))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> adminAuthService.requireAdmin(response.getAdminToken()));
    }

    @Test
    void logoutShouldInvalidateToken() {
        adminAuthProperties.setSessionTtlMinutes(120);
        adminAuthProperties.setSessionKeyPrefix("smart-life:admin:session:");
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setDisplayName("系统管理员");
        admin.setStatus(Admin.AdminStatus.ACTIVE);
        admin.setPasswordHash("encoded-password");

        when(adminRepository.findByUsernameAndDeletedFalse("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin123", "encoded-password")).thenReturn(true);

        AdminLoginResponse response = adminAuthService.login("admin", "admin123");
        adminAuthService.logout(response.getAdminToken());

        verify(stringRedisTemplate).delete("smart-life:admin:session:" + response.getAdminToken());
    }

    @Test
    void requireAdminShouldLoadAdminFromRedisSession() {
        adminAuthProperties.setSessionTtlMinutes(120);
        adminAuthProperties.setSessionKeyPrefix("smart-life:admin:session:");
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setStatus(Admin.AdminStatus.ACTIVE);
        when(valueOperations.get("smart-life:admin:session:token-123")).thenReturn("1");
        when(stringRedisTemplate.expire("smart-life:admin:session:token-123", Duration.ofMinutes(120))).thenReturn(true);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        Admin loaded = adminAuthService.requireAdmin("token-123");

        assertEquals(1L, loaded.getId());
    }

    private static final class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zoneId;

        private MutableClock(Instant instant, ZoneId zoneId) {
            this.instant = instant;
            this.zoneId = zoneId;
        }

        @Override
        public ZoneId getZone() {
            return zoneId;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }

    }
}
