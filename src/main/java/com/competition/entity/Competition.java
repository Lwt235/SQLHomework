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
    private LocalDateTime awardPublishStart; // Also review end
    private Integer maxTeamSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
    
    // Computed properties for backward compatibility and convenience
    public LocalDateTime getSignupEnd() {
        return submitStart;
    }
    
    public void setSignupEnd(LocalDateTime signupEnd) {
        // For backward compatibility: setting signupEnd sets submitStart
        this.submitStart = signupEnd;
    }
    
    public LocalDateTime getStartDate() {
        return submitStart;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        // For backward compatibility: setting startDate sets submitStart
        this.submitStart = startDate;
    }
    
    public LocalDateTime getReviewEnd() {
        return awardPublishStart;
    }
    
    public void setReviewEnd(LocalDateTime reviewEnd) {
        // For backward compatibility: setting reviewEnd sets awardPublishStart
        this.awardPublishStart = reviewEnd;
    }
    
    public LocalDateTime getAwardPublishDate() {
        return awardPublishStart;
    }
    
    public void setAwardPublishDate(LocalDateTime awardPublishDate) {
        // For backward compatibility: setting awardPublishDate sets awardPublishStart
        this.awardPublishStart = awardPublishDate;
    }
    
    public LocalDateTime getEndDate() {
        if (awardPublishStart != null) {
            return awardPublishStart.plusDays(1);
        }
        return null;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        // For backward compatibility: setting endDate sets awardPublishStart to 1 day before
        if (endDate != null) {
            this.awardPublishStart = endDate.minusDays(1);
        }
    }
}
