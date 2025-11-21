package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Competition")
public class Competition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "competition_id")
    private Integer competitionId;
    
    @Column(name = "competition_title", nullable = false, length = 255)
    private String competitionTitle;
    
    @Column(name = "short_title", length = 100)
    private String shortTitle;
    
    @Column(name = "competition_status", length = 20)
    private String competitionStatus = "draft";
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "level", nullable = false, length = 20)
    private String level = "school";
    
    @Column(name = "organizer", length = 200)
    private String organizer;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "signup_start")
    private LocalDateTime signupStart;
    
    @Column(name = "signup_end")
    private LocalDateTime signupEnd;
    
    @Column(name = "submit_start")
    private LocalDateTime submitStart;
    
    @Column(name = "submit_end")
    private LocalDateTime submitEnd;
    
    @Column(name = "review_start")
    private LocalDateTime reviewStart;
    
    @Column(name = "review_end")
    private LocalDateTime reviewEnd;
    
    @Column(name = "award_publish_date")
    private LocalDateTime awardPublishDate;
    
    @Column(name = "max_team_size")
    private Integer maxTeamSize;
    
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
