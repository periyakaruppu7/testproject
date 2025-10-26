package com.example.lms.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.lms.model.User;
import com.example.lms.service.UserService;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Create demo users if they don't exist
        if (userService.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", "password", "admin@lms.com", "ADMIN");
            userService.saveUser(admin);
        }
        if (userService.findByUsername("instructor").isEmpty()) {
            User instructor = new User("instructor", "password", "instructor@lms.com", "INSTRUCTOR");
            userService.saveUser(instructor);
        }
        if (userService.findByUsername("student").isEmpty()) {
            User student = new User("student", "password", "student@lms.com", "STUDENT");
            userService.saveUser(student);
        }
    }
}