package com.yxtech.smartlife.controller.adminweb;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.adminweb.QrCodeStatusRequest;
import com.yxtech.smartlife.service.AdminAuthService;
import com.yxtech.smartlife.service.AdminWebQrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-web")
@RequiredArgsConstructor
public class AdminWebAuthController {

    private final AdminWebQrCodeService qrCodeService;
    private final AdminAuthService adminAuthService;

    @PostMapping("/qrcode")
    public Result<Map<String, Object>> getQrCode() {
        return Result.success(qrCodeService.generateQrCode());
    }

    @PostMapping("/qrcode/status")
    public Result<Map<String, Object>> checkQrCodeStatus(@RequestBody QrCodeStatusRequest request) {
        return Result.success(qrCodeService.checkStatus(request.getTicket()));
    }

    @PostMapping("/qrcode/confirm")
    public Result<Void> confirmQrCode(@RequestBody Map<String, Object> body) {
        String ticket = (String) body.get("ticket");
        Long adminId = body.get("adminId") != null ? Long.valueOf(body.get("adminId").toString()) : 1L;
        qrCodeService.confirmLogin(ticket, adminId);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            adminAuthService.logout(token);
        }
        return Result.success();
    }
}
