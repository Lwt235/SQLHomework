package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Award {
    
    private Integer awardId;
    private Integer competitionId;
    private String awardName;
    private String awardLevel = "none";
    private String criteriaDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
