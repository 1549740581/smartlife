package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByOpenId(String openId);

    User updateUser(User user);

    void deleteUser(Long id);

    List<User> findAllUsers();

    List<User> findUsersByStatus(User.UserStatus status);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User loginOrRegisterWechatUser(String openId, String nickname, String avatarUrl);
}
