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

import com.example.quizapp.entity.Question;
import com.example.quizapp.service.QuestionService;

@RestController
@RequestMapping("/api/questions")
public class QuestionRestController {
    @Autowired
    private QuestionService questionService;

    @PostMapping("/quiz/{quizId}")
    public Question addQuestion(@PathVariable Long quizId, @RequestBody Question question) {
        return questionService.addQuestionToQuiz(quizId, question);
    }

    @GetMapping("/quiz/{quizId}")
    public List<Question> getQuestion(@PathVariable Long quizId) {
        return questionService.getQuestionsByQuizId(quizId);
    }

    @PutMapping("/quiz/{quizId}")
    public Question updateQuestion(@PathVariable Long quizId, @RequestBody Question updatedQuestion) {
        return questionService.updateQuestion(quizId, updatedQuestion);
    }
    @DeleteMapping("/quiz/{questionId}")
   public ResponseEntity<String> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.ok("Question deleted successfully!");
    }

}
