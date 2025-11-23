package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Rule {
    
    private Integer ruleId;
    private Integer competitionId;
    private String ruleType = "other";
    private String content;
    private Integer versionNo;
    private Boolean isActive = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
