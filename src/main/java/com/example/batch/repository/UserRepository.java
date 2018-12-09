package com.example.batch.repository;

import com.example.batch.domain.User;
import com.example.batch.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by gavinkim at 2018-12-09
 */
public interface UserRepository extends JpaRepository<User,Long> {
   List<User> findByUpdatedDateBeforeAndStatusEquals(LocalDateTime localDateTime, UserStatus status);
}
