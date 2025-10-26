package com.example.quizapp.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.quizapp.entity.Question;
import com.example.quizapp.entity.Quiz;
import com.example.quizapp.repository.QuestionRepository;
import com.example.quizapp.repository.QuizRepository;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    public Question addQuestionToQuiz(Long quizId, Question question) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id " + quizId));

        // Link question to quiz
        question.setQuiz(quiz);

        // Save question directly — DO NOT add to quiz.getQuestions()
        return questionRepository.save(question);
    }

    // / NEW: get all questions by quiz ID
    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getQuiz().getId().equals(quizId))
                .toList();
    }

    // Calculate score
    public int calculateScore(Long quizId, Map<Long, String> submittedAnswers) {
        List<Question> questions = getQuestionsByQuizId(quizId);
        int score = 0;
        for (Question q : questions) {
            String submitted = submittedAnswers.get(q.getId());
            if (submitted != null && submitted.equalsIgnoreCase(q.getAnswer())) {
                score++;
            }
        }
        return score;
    }

    public Question updateQuestion(Long quizId, Question updatedQuestion) {
        return questionRepository.findById(quizId).map(question -> {
            question.setContent(updatedQuestion.getContent());
            question.setOptionA(updatedQuestion.getOptionA());
            question.setOptionB(updatedQuestion.getOptionB());
            question.setOptionC(updatedQuestion.getOptionC());
            question.setOptionD(updatedQuestion.getOptionD());
            question.setAnswer(updatedQuestion.getAnswer());
            return questionRepository.save(question);
        }).orElseThrow(() -> new RuntimeException("Question not found with id " + quizId));
    }

    public void deleteQuestion(Long questionId) {
    if (!questionRepository.existsById(questionId)) {
        throw new RuntimeException("Question not found with id: " + questionId);
    }
    questionRepository.deleteById(questionId);
}

}
