package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.exception.ConflictException;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserShouldRejectDuplicateUsername() {
        User user = new User();
        user.setUsername("tester");

        when(userRepository.existsByUsernameAndDeletedFalse("tester")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(user));
    }

    @Test
    void loginOrRegisterWechatUserShouldCreateUserWhenOpenIdNotFound() {
        when(userRepository.findByOpenIdAndDeletedFalse("wx-code")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User user = userService.loginOrRegisterWechatUser("wx-code", "张三", null);

        assertEquals(1L, user.getId());
        assertEquals("wx-code", user.getOpenId());
        assertEquals("张三", user.getNickname());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }
}
