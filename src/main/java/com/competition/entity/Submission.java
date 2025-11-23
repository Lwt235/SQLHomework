package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Submission {
    
    private Integer submissionId;
    private Integer registrationId;
    private String submissionTitle;
    private String abstractText;
    private String submissionStatus = "draft";
    private LocalDateTime submittedAt;
    private LocalDateTime finalLockedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
