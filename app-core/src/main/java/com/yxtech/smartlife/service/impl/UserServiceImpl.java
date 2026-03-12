package com.yxtech.smartlife.service.impl;

import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.exception.ConflictException;
import com.yxtech.smartlife.exception.NotFoundException;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(User user) {
        validateCreate(user);
        if (user.getStatus() == null) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email);
    }

    @Override
    public Optional<User> findByOpenId(String openId) {
        return userRepository.findByOpenIdAndDeletedFalse(openId);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User existing = userRepository.findByIdAndDeletedFalse(user.getId())
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (StringUtils.hasText(user.getUsername())
                && userRepository.existsByUsernameAndDeletedFalseAndIdNot(user.getUsername(), existing.getId())) {
            throw new ConflictException("username already exists");
        }
        if (StringUtils.hasText(user.getEmail())
                && userRepository.existsByEmailAndDeletedFalseAndIdNot(user.getEmail(), existing.getId())) {
            throw new ConflictException("email already exists");
        }

        existing.setUsername(user.getUsername());
        existing.setPassword(user.getPassword());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setNickname(user.getNickname());
        existing.setAvatarUrl(user.getAvatarUrl());
        existing.setStatus(user.getStatus() == null ? existing.getStatus() : user.getStatus());
        return userRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User existing = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("user not found"));
        existing.setDeleted(true);
        userRepository.save(existing);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findByDeletedFalse();
    }

    @Override
    public List<User> findUsersByStatus(User.UserStatus status) {
        return userRepository.findByStatusAndDeletedFalse(status);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameAndDeletedFalse(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndDeletedFalse(email);
    }

    @Override
    @Transactional
    public User loginOrRegisterWechatUser(String openId, String nickname, String avatarUrl) {
        return userRepository.findByOpenIdAndDeletedFalse(openId)
                .map(existing -> {
                    if (StringUtils.hasText(nickname)) {
                        existing.setNickname(nickname);
                    }
                    if (StringUtils.hasText(avatarUrl)) {
                        existing.setAvatarUrl(avatarUrl);
                    }
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setOpenId(openId);
                    user.setUsername("wx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
                    user.setNickname(StringUtils.hasText(nickname) ? nickname : "微信用户");
                    user.setAvatarUrl(avatarUrl);
                    user.setStatus(User.UserStatus.ACTIVE);
                    return userRepository.save(user);
                });
    }

    private void validateCreate(User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (userRepository.existsByUsernameAndDeletedFalse(user.getUsername())) {
            throw new ConflictException("username already exists");
        }
        if (StringUtils.hasText(user.getEmail()) && userRepository.existsByEmailAndDeletedFalse(user.getEmail())) {
            throw new ConflictException("email already exists");
        }
    }
}
