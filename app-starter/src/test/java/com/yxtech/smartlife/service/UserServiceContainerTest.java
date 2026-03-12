package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.exception.ConflictException;
import com.yxtech.smartlife.repository.UserRepository;
import com.yxtech.smartlife.support.AbstractContainerIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceContainerTest extends AbstractContainerIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAllInBatch();
    }

    @Test
    void createUserShouldPersistIntoMysql() {
        User user = new User();
        user.setUsername("container-user");
        user.setEmail("container-user@test.com");
        user.setNickname("容器用户");

        User saved = userService.createUser(user);

        assertNotNull(saved.getId());
        assertEquals(User.UserStatus.ACTIVE, saved.getStatus());
        assertEquals("container-user", userRepository.findByUsernameAndDeletedFalse("container-user")
                .orElseThrow()
                .getUsername());
    }

    @Test
    void createUserShouldRejectDuplicateUsernameInMysql() {
        User existing = new User();
        existing.setUsername("duplicate-user");
        existing.setEmail("duplicate-user@test.com");
        existing.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(existing);

        User duplicated = new User();
        duplicated.setUsername("duplicate-user");
        duplicated.setEmail("another@test.com");

        assertThrows(ConflictException.class, () -> userService.createUser(duplicated));
    }
}
