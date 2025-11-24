package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Competition {
    
    private Integer competitionId;
    private String competitionTitle;
    private String shortTitle;
    private String competitionStatus = "draft";
    private String description;
    private String category;
    private String level = "school";
    private String organizer;
    private LocalDateTime signupStart;
    private LocalDateTime submitStart; // Also signup end and competition start
    private LocalDateTime submitEnd; // Also review start
    private LocalDateTime awardPublishStart; // Also review end
    private Integer maxTeamSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
    
    // Computed properties for backward compatibility and convenience
    public LocalDateTime getSignupEnd() {
        return submitStart;
    }
    
    public LocalDateTime getStartDate() {
        return submitStart;
    }
    
    public LocalDateTime getReviewStart() {
        return submitEnd;
    }
    
    public LocalDateTime getReviewEnd() {
        return awardPublishStart;
    }
    
    public LocalDateTime getAwardPublishDate() {
        return awardPublishStart;
    }
    
    public LocalDateTime getEndDate() {
        if (awardPublishStart != null) {
            return awardPublishStart.plusDays(1);
        }
        return null;
    }
}
