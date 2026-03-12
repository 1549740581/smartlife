package com.yxtech.smartlife.auth;

import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    private final AdminAuthService adminAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(ADMIN_TOKEN_HEADER);
        Admin admin = adminAuthService.requireAdmin(token);
        request.setAttribute(AdminRequestAttributes.CURRENT_ADMIN_ATTRIBUTE, admin);
        request.setAttribute(AdminRequestAttributes.ADMIN_TOKEN_ATTRIBUTE, token);
        return true;
    }
}
