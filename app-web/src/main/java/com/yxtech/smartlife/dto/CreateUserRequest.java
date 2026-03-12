package com.yxtech.smartlife.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank
    private String username;

    private String password;

    @Email
    private String email;

    private String phone;

    private String nickname;
}
