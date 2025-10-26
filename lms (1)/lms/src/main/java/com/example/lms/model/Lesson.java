package com.example.lms.model;
// Lesson.java

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String content;
    private String videoUrl; // YouTube URL
    private String pdfFileName; // Store PDF file name
    private String pdfFileUrl; // Store PDF file path/URL
    
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    
    // Constructors, Getters, Setters
    public Lesson() {}
    
    public Lesson(String title, String content, String videoUrl, String pdfFileName, String pdfFileUrl, Course course) {
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.pdfFileName = pdfFileName;
        this.pdfFileUrl = pdfFileUrl;
        this.course = course;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getPdfFileName() { return pdfFileName; }
    public void setPdfFileName(String pdfFileName) { this.pdfFileName = pdfFileName; }
    public String getPdfFileUrl() { return pdfFileUrl; }
    public void setPdfFileUrl(String pdfFileUrl) { this.pdfFileUrl = pdfFileUrl; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}