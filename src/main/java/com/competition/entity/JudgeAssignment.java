package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
@Entity
@Table(name = "JudgeAssignment")
@IdClass(JudgeAssignment.JudgeAssignmentId.class)
public class JudgeAssignment {
    
    @Id
    @Column(name = "user_id")
    private Integer userId;
    
    @Id
    @Column(name = "submission_id")
    private Integer submissionId;
    
    @Column(name = "weight", nullable = false, precision = 8, scale = 2)
    private BigDecimal weight;
    
    @Column(name = "score", nullable = false, precision = 8, scale = 2)
    private BigDecimal score;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Data
    public static class JudgeAssignmentId implements Serializable {
        private Integer userId;
        private Integer submissionId;
    }
}
