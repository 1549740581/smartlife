package com.yxtech.smartlife.auth;

import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.exception.UnauthorizedException;
import com.yxtech.smartlife.service.AdminAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAuthInterceptorTest {

    private final AdminAuthService adminAuthService = mock(AdminAuthService.class);
    private final AdminAuthInterceptor interceptor = new AdminAuthInterceptor(adminAuthService);

    @Test
    void preHandleShouldStoreCurrentAdminAndToken() {
        Admin admin = new Admin();
        admin.setId(2L);
        admin.setUsername("admin");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AdminAuthInterceptor.ADMIN_TOKEN_HEADER, "token-123");
        when(adminAuthService.requireAdmin("token-123")).thenReturn(admin);

        boolean handled = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(handled);
        assertEquals(admin, request.getAttribute(AdminRequestAttributes.CURRENT_ADMIN_ATTRIBUTE));
        assertEquals("token-123", request.getAttribute(AdminRequestAttributes.ADMIN_TOKEN_ATTRIBUTE));
        verify(adminAuthService).requireAdmin("token-123");
    }

    @Test
    void preHandleShouldRejectMissingToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(adminAuthService.requireAdmin(null)).thenThrow(new UnauthorizedException("admin token invalid"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object())
        );

        assertEquals("admin token invalid", exception.getMessage());
        verify(adminAuthService).requireAdmin(null);
    }
}
