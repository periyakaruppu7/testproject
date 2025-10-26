package com.example.lms.controller;
// MainController.java


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.model.Course;
import com.example.lms.model.Lesson;
import com.example.lms.model.User;
import com.example.lms.service.CourseService;
import com.example.lms.service.LessonService;
import com.example.lms.service.UserService;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

@Controller
public class MainController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private LessonService lessonService;
    
    @Autowired
    private UserService userService;
    
    // Add this constant for file storage
    private final String UPLOAD_DIR = "./uploads/";
    
    // Home page
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", currentUser.getRole());
        }
        model.addAttribute("courses", courseService.getAllCourses());
        return "index";
    }
    
    // Show signup form
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }
    
    // Process signup
    @PostMapping("/signup")
    public String processSignup(@ModelAttribute User user, HttpSession session, Model model) {
        try {
            // Check if username already exists
            if (userService.findByUsername(user.getUsername()).isPresent()) {
                model.addAttribute("error", "Username already exists");
                return "signup";
            }
            
            // Check if email already exists
            if (userService.findByEmail(user.getEmail()).isPresent()) {
                model.addAttribute("error", "Email already exists");
                return "signup";
            }
            
            // Save new user
            User savedUser = userService.saveUser(user);
            
            // Auto-login after signup
            session.setAttribute("currentUser", savedUser);
            session.setAttribute("userRole", savedUser.getRole());
            
            return "redirect:/";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error creating account: " + e.getMessage());
            return "signup";
        }
    }
    
    // Show login form
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }
    
    // Process login
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                             @RequestParam String password,
                             HttpSession session, 
                             Model model) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            // Set user in session
            session.setAttribute("currentUser", user.get());
            session.setAttribute("userRole", user.get().getRole());
            return "redirect:/";
        } else {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("user", new User());
            return "login";
        }
    }
    
    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("currentUser");
        session.removeAttribute("userRole");
        return "redirect:/";
    }
    
    // Student portal - view courses and lessons
    @GetMapping("/student/courses")
    public String studentCourses(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!currentUser.getRole().equals("STUDENT")) {
            model.addAttribute("error", "Access denied. Student role required.");
            return "redirect:/";
        }
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("courses", courseService.getAllCourses());
        return "student/courses";
    }
    
    @GetMapping("/student/courses/{courseId}/lessons")
    public String viewCourseLessons(@PathVariable Long courseId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!currentUser.getRole().equals("STUDENT")) {
            model.addAttribute("error", "Access denied. Student role required.");
            return "redirect:/";
        }
        
        Optional<Course> course = courseService.getCourseById(courseId);
        if (course.isPresent()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("course", course.get());
            model.addAttribute("lessons", lessonService.getLessonsByCourse(courseId));
            return "student/lessons";
        }
        return "redirect:/student/courses";
    }
    
    // Admin/Instructor - Course Management
    @GetMapping("/admin/courses")
    public String adminCourses(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("INSTRUCTOR")) {
            model.addAttribute("error", "Access denied. Admin or Instructor role required.");
            return "redirect:/";
        }
        
        // Admin sees all courses, Instructor sees only their courses
        List<Course> courses;
        if (currentUser.getRole().equals("ADMIN")) {
            courses = courseService.getAllCourses();
        } else {
            courses = courseService.getCoursesByInstructor(currentUser.getId());
        }
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("courses", courses);
        model.addAttribute("course", new Course());
        model.addAttribute("instructors", userService.getAllUsers().stream()
                .filter(u -> u.getRole().equals("INSTRUCTOR") || u.getRole().equals("ADMIN"))
                .collect(Collectors.toList()));
        
        return "admin/courses";
    }
    
    @PostMapping("/admin/courses")
    public String saveCourse(@ModelAttribute Course course, 
                            @RequestParam(required = false) Long instructorId,
                            HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("INSTRUCTOR"))) {
            return "redirect:/login";
        }
        
        // Set instructor: Admin can assign any instructor, Instructor can only assign themselves
        if (currentUser.getRole().equals("ADMIN") && instructorId != null) {
            Optional<User> instructor = userService.getUserById(instructorId);
            instructor.ifPresent(course::setInstructor);
        } else {
            course.setInstructor(currentUser);
        }
        
        courseService.saveCourse(course);
        return "redirect:/admin/courses";
    }
    
    @GetMapping("/admin/courses/edit/{id}")
    public String editCourseForm(@PathVariable Long id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("INSTRUCTOR")) {
            model.addAttribute("error", "Access denied.");
            return "redirect:/";
        }
        
        Optional<Course> course = courseService.getCourseById(id);
        if (course.isPresent()) {
            // Check permissions: Instructor can only edit their own courses
            if (currentUser.getRole().equals("INSTRUCTOR") && 
                !course.get().getInstructor().getId().equals(currentUser.getId())) {
                model.addAttribute("error", "You can only edit your own courses.");
                return "redirect:/admin/courses";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("course", course.get());
            model.addAttribute("instructors", userService.getAllUsers().stream()
                    .filter(u -> u.getRole().equals("INSTRUCTOR") || u.getRole().equals("ADMIN"))
                    .collect(Collectors.toList()));
            return "admin/edit-course";
        }
        return "redirect:/admin/courses";
    }
    
    @PostMapping("/admin/courses/update/{id}")
    public String updateCourse(@PathVariable Long id, 
                              @ModelAttribute Course course,
                              @RequestParam(required = false) Long instructorId,
                              HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Course> existingCourse = courseService.getCourseById(id);
        if (existingCourse.isPresent()) {
            // Check permissions
            if (currentUser.getRole().equals("INSTRUCTOR") && 
                !existingCourse.get().getInstructor().getId().equals(currentUser.getId())) {
                return "redirect:/admin/courses";
            }
            
            // Update course details
            Course courseToUpdate = existingCourse.get();
            courseToUpdate.setTitle(course.getTitle());
            courseToUpdate.setDescription(course.getDescription());
            
            // Only admin can change instructor
            if (currentUser.getRole().equals("ADMIN") && instructorId != null) {
                Optional<User> instructor = userService.getUserById(instructorId);
                instructor.ifPresent(courseToUpdate::setInstructor);
            }
            
            courseService.saveCourse(courseToUpdate);
        }
        return "redirect:/admin/courses";
    }
    
    @GetMapping("/admin/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Course> course = courseService.getCourseById(id);
        if (course.isPresent()) {
            // Check permissions: Instructor can only delete their own courses
            if (currentUser.getRole().equals("INSTRUCTOR") && 
                !course.get().getInstructor().getId().equals(currentUser.getId())) {
                model.addAttribute("error", "You can only delete your own courses.");
                return "redirect:/admin/courses";
            }
            
            courseService.deleteCourse(id);
        }
        return "redirect:/admin/courses";
    }
    
    // Enhanced Lesson Management with File Upload
    @GetMapping("/admin/courses/{courseId}/lessons")
    public String manageLessons(@PathVariable Long courseId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("INSTRUCTOR")) {
            model.addAttribute("error", "Access denied. Admin or Instructor role required.");
            return "redirect:/";
        }
        
        Optional<Course> course = courseService.getCourseById(courseId);
        if (course.isPresent()) {
            // Check if the course belongs to the current instructor or if user is admin
            if (!currentUser.getRole().equals("ADMIN") && !course.get().getInstructor().getId().equals(currentUser.getId())) {
                model.addAttribute("error", "Access denied. You can only manage your own courses.");
                return "redirect:/admin/courses";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("course", course.get());
            model.addAttribute("lessons", lessonService.getLessonsByCourse(courseId));
            model.addAttribute("lesson", new Lesson());
            return "admin/lessons";
        }
        return "redirect:/admin/courses";
    }
    
    // Enhanced Save Lesson with File Upload
    @PostMapping("/admin/courses/{courseId}/lessons")
    public String saveLesson(@PathVariable Long courseId, 
                           @ModelAttribute Lesson lesson,
                           @RequestParam("pdfFile") MultipartFile pdfFile,
                           HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("INSTRUCTOR"))) {
            return "redirect:/login";
        }
        
        Optional<Course> course = courseService.getCourseById(courseId);
        if (course.isPresent()) {
            // Check if the course belongs to the current instructor or if user is admin
            if (!currentUser.getRole().equals("ADMIN") && !course.get().getInstructor().getId().equals(currentUser.getId())) {
                return "redirect:/admin/courses";
            }
            
            // Handle PDF file upload
            if (!pdfFile.isEmpty()) {
                try {
                    // Create upload directory if it doesn't exist
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    
                    // Generate unique file name
                    String fileName = System.currentTimeMillis() + "_" + pdfFile.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(pdfFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Set PDF file info in lesson
                    lesson.setPdfFileName(pdfFile.getOriginalFilename());
                    lesson.setPdfFileUrl("/uploads/" + fileName);
                    
                } catch (IOException e) {
                    // Handle file upload error
                    e.printStackTrace();
                }
            }
            
            lesson.setCourse(course.get());
            lessonService.saveLesson(lesson);
        }
        return "redirect:/admin/courses/" + courseId + "/lessons";
    }
    
    @GetMapping("/admin/lessons/delete/{id}")
    public String deleteLesson(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("INSTRUCTOR"))) {
            return "redirect:/login";
        }
        
        Optional<Lesson> lesson = lessonService.getLessonById(id);
        if (lesson.isPresent()) {
            Course course = lesson.get().getCourse();
            // Check if the course belongs to the current instructor or if user is admin
            if (currentUser.getRole().equals("ADMIN") || course.getInstructor().getId().equals(currentUser.getId())) {
                Long courseId = course.getId();
                lessonService.deleteLesson(id);
                return "redirect:/admin/courses/" + courseId + "/lessons";
            }
        }
        return "redirect:/admin/courses";
    }
    
    // Add method to serve uploaded files
    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public org.springframework.core.io.Resource serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }
    
    // User Management (Admin only)
    @GetMapping("/admin/users")
    public String manageUsers(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!currentUser.getRole().equals("ADMIN")) {
            model.addAttribute("error", "Access denied. Admin role required.");
            return "redirect:/";
        }
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
    
    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }
        
        // Prevent admin from deleting themselves
        if (!id.equals(currentUser.getId())) {
            userService.deleteUser(id);
        }
        
        return "redirect:/admin/users";
    }
    
    // Course Statistics (Admin only)
    @GetMapping("/admin/courses/stats")
    public String courseStats(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            model.addAttribute("error", "Access denied. Admin role required.");
            return "redirect:/";
        }
        
        List<Course> allCourses = courseService.getAllCourses();
        Map<String, Long> coursesByInstructor = allCourses.stream()
                .collect(Collectors.groupingBy(
                    course -> course.getInstructor().getUsername(),
                    Collectors.counting()
                ));
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalCourses", allCourses.size());
        model.addAttribute("coursesByInstructor", coursesByInstructor);
        model.addAttribute("instructors", userService.getAllUsers().stream()
                .filter(u -> u.getRole().equals("INSTRUCTOR"))
                .collect(Collectors.toList()));
        
        return "admin/course-stats";
    }
}