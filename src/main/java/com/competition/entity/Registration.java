package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Registration {
    
    private Integer registrationId;
    private Integer competitionId;
    private Integer teamId;
    private Integer userId;
    private Integer auditUserId;
    private String registrationStatus = "pending";
    private LocalDateTime auditTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
