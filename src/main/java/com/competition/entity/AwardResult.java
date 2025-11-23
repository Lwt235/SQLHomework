package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AwardResult {
    
    private Integer registrationId;
    private Integer awardId;
    private LocalDateTime awardTime;
    private String certificateNo;
    private String certificatePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
