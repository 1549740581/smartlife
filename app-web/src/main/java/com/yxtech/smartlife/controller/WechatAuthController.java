package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.WechatLoginRequest;
import com.yxtech.smartlife.dto.WechatLoginResponse;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wechat")
@RequiredArgsConstructor
public class WechatAuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<WechatLoginResponse> login(@Valid @RequestBody WechatLoginRequest request) {
        User user = userService.loginOrRegisterWechatUser(request.getCode(), request.getNickname(), request.getAvatarUrl());
        WechatLoginResponse response = new WechatLoginResponse();
        response.setUserId(user.getId());
        response.setOpenId(user.getOpenId());
        response.setNickname(user.getNickname());
        return Result.success(response);
    }
}
