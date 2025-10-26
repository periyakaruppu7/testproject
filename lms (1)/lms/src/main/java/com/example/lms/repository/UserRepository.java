package com.example.lms.repository;
// UserRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
