package com.example.lms.service;
// LessonService.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lms.model.Lesson;
import com.example.lms.repository.LessonRepository;

import java.util.List;
import java.util.Optional;

@Service
public class LessonService {
    @Autowired
    private LessonRepository lessonRepository;
    
    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }
    
    public Optional<Lesson> getLessonById(Long id) {
        return lessonRepository.findById(id);
    }
    
    public Lesson saveLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }
    
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }
    
    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseId(courseId);
    }
}
