package com.example.quizapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quizapp.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);

}
