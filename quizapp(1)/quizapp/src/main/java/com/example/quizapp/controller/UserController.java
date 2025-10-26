package com.example.quizapp.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.quizapp.entity.Quiz;
import com.example.quizapp.entity.User;
import com.example.quizapp.service.EmailService;
import com.example.quizapp.service.QuestionService;
import com.example.quizapp.service.QuizService;
import com.example.quizapp.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private QuestionService questionService;

    // ==============================
    // SIGNUP
    // ==============================
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        // Encode password before saving
        // Save user
        userService.saveUser(user);
        // Send email notification
        String subject = "Registration Successful";
        String text = "Hi " + user.getUsername() + ",\n\n" +
                "You have registered successfully on QuizApp!\n\n" +
                "Thank you for joining us.";
        emailService.sendSimpleEmail(user.getEmail(), subject, text);

        // Redirect to login
        return "redirect:/login?registered=true";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        boolean isValid = userService.validateUser(username, password);

        if (isValid) {
            User user = userService.findByUsername(username);
            session.setAttribute("loggedInUser", user);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    @GetMapping("/home")
    public String homePage(Model model, HttpSession session) {
        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            // Not logged in → redirect to login page
            return "redirect:/login";
        }

        // Logged in → show home page
        List<Quiz> quizzes = quizService.getAllQuizzes();
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("user", loggedInUser); // optional, to show username in UI
        return "home";
    }

    @GetMapping("/quiz/{quizId}")
    public String showQuizQuestions(@PathVariable Long quizId, Model model) {
        Quiz quiz = quizService.getQuizById(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", quiz.getQuestions());
        return "quiz-questions";
    }

    @PostMapping("/quiz/{quizId}/submit")
    public String submitQuiz(@PathVariable Long quizId,
            @RequestParam Map<String, String> allParams,
            Model model) {

        // Convert request param keys to Long (question IDs)
        Map<Long, String> submittedAnswers = allParams.entrySet().stream()
                .filter(entry -> entry.getKey().matches("\\d+")) // Only numeric keys
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey()),
                        Map.Entry::getValue));

        // Calculate score
        int score = questionService.calculateScore(quizId, submittedAnswers);

        // Fetch quiz for title
        Quiz quiz = quizService.getQuizById(quizId);

        model.addAttribute("score", score);
        model.addAttribute("total", submittedAnswers.size());
        model.addAttribute("quiz", quiz);

        return "quiz-score";
    }
}
