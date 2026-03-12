package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.CreateUserRequest;
import com.yxtech.smartlife.dto.UserDTO;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.exception.NotFoundException;
import com.yxtech.smartlife.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Result<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        User saved = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(UserDTO.fromEntity(saved)));
    }

    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable("id") Long id) {
        return Result.success(UserDTO.fromEntity(
                userService.findById(id).orElseThrow(() -> new NotFoundException("user not found"))
        ));
    }

    @GetMapping
    public Result<List<UserDTO>> getAllUsers() {
        return Result.success(userService.findAllUsers().stream().map(UserDTO::fromEntity).toList());
    }

    @GetMapping("/username/{username}")
    public Result<UserDTO> getUserByUsername(@PathVariable("username") String username) {
        return Result.success(UserDTO.fromEntity(
                userService.findByUsername(username).orElseThrow(() -> new NotFoundException("user not found"))
        ));
    }

    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(@PathVariable("id") Long id, @Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setId(id);
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        return Result.success(UserDTO.fromEntity(userService.updateUser(user)));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return Result.success("deleted", "ok");
    }

    @GetMapping("/status/{status}")
    public Result<List<UserDTO>> getUsersByStatus(@PathVariable("status") User.UserStatus status) {
        return Result.success(userService.findUsersByStatus(status).stream().map(UserDTO::fromEntity).toList());
    }

    @GetMapping("/check/username/{username}")
    public Result<Map<String, Boolean>> checkUsernameExists(@PathVariable("username") String username) {
        return Result.success(Map.of("exists", userService.existsByUsername(username)));
    }

    @GetMapping("/check/email/{email}")
    public Result<Map<String, Boolean>> checkEmailExists(@PathVariable("email") String email) {
        return Result.success(Map.of("exists", userService.existsByEmail(email)));
    }
}
