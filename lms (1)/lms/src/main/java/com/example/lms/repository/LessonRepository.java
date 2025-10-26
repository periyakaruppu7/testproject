package com.example.lms.repository;
// LessonRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.model.Lesson;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseId(Long courseId);
}