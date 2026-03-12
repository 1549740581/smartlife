package com.yxtech.smartlife.repository;

import com.yxtech.smartlife.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByPhoneAndDeletedFalse(String phone);

    Optional<User> findByOpenIdAndDeletedFalse(String openId);

    List<User> findByDeletedFalse();

    List<User> findByStatusAndDeletedFalse(User.UserStatus status);

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByUsernameAndDeletedFalseAndIdNot(String username, Long id);

    boolean existsByEmailAndDeletedFalseAndIdNot(String email, Long id);
}
