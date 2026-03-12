package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.auth.AdminRequestAttributes;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.AdminLoginRequest;
import com.yxtech.smartlife.dto.AdminLoginResponse;
import com.yxtech.smartlife.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success(adminAuthService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute(AdminRequestAttributes.ADMIN_TOKEN_ATTRIBUTE) String token) {
        adminAuthService.logout(token);
        return Result.success();
    }
}
