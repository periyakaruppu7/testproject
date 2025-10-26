package com.example.quizapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.quizapp.entity.User;
import com.example.quizapp.repository.UserRepository;
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Save new user
    public void saveUser(User user) {
        // Optional: Encrypt password later if you add security
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER"); // default role
        }
        userRepository.save(user);
    }
    // Validate login credentials
    public boolean validateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // Get user by ID
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }
    // Delete user by ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    // Find user by username
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
