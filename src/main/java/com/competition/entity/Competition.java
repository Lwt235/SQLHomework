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
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime signupStart;
    private LocalDateTime signupEnd;
    private LocalDateTime submitStart;
    private LocalDateTime submitEnd;
    private LocalDateTime reviewStart;
    private LocalDateTime reviewEnd;
    private LocalDateTime awardPublishDate;
    private Integer maxTeamSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
