package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Award")
public class Award {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "award_id")
    private Integer awardId;
    
    @Column(name = "competition_id", nullable = false)
    private Integer competitionId;
    
    @Column(name = "award_name", nullable = false, length = 200)
    private String awardName;
    
    @Column(name = "award_level", nullable = false, length = 50)
    private String awardLevel = "none";
    
    @Column(name = "criteria_description", columnDefinition = "TEXT")
    private String criteriaDescription;
    
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
}
