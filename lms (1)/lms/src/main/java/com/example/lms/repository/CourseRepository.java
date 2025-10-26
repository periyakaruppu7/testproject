package com.example.lms.repository;
// CourseRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.model.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);
}