package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Submission")
public class Submission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer submissionId;
    
    @Column(name = "registration_id", nullable = false)
    private Integer registrationId;
    
    @Column(name = "submission_title", nullable = false, length = 255)
    private String submissionTitle;
    
    @Column(name = "abstract", columnDefinition = "TEXT")
    private String abstractText;
    
    @Column(name = "submission_status", nullable = false, length = 20)
    private String submissionStatus = "draft";
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "final_locked_at")
    private LocalDateTime finalLockedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
