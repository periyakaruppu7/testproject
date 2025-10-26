package com.example.quizapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quizapp.entity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz,Long>{
    Optional<Quiz> findByQuizname(String quizname);
    
}
