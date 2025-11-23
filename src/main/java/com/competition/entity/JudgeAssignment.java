package com.competition.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JudgeAssignment {
    
    private Integer userId;
    private Integer submissionId;
    private BigDecimal weight;
    private BigDecimal score;
    private String comment;
    private String judgeStatus = "pending";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
