package com.example.quizapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quizapp.entity.Quiz;
import com.example.quizapp.service.QuizService;

@RestController
@RequestMapping("/api/quizzes")
public class QuizRestController {

    @Autowired
    private QuizService quizService;

    // Get all quizzes
    @GetMapping
    public List<Quiz> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    // Get quiz by ID
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        Quiz quiz = quizService.getQuizById(id);
        if (quiz != null) {
            return ResponseEntity.ok(quiz);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create new quiz
    @PostMapping
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        Quiz savedQuiz = quizService.saveQuiz(quiz);
        return ResponseEntity.ok(savedQuiz);
    }

    // Update existing quiz
    @PutMapping("/{id}")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quizDetails) {
        Quiz quiz = quizService.getQuizById(id);
        if (quiz == null) {
            return ResponseEntity.notFound().build();
        }
        quiz.setQuizname(quizDetails.getQuizname());
        quiz.setDescription(quizDetails.getDescription());
        quiz.setNumberOfQuestion(quizDetails.getNumberOfQuestion());
        quiz.setMarks(quizDetails.getMarks());

        Quiz updatedQuiz = quizService.saveQuiz(quiz);
        return ResponseEntity.ok(updatedQuiz);
    }

    // Delete quiz
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
}
