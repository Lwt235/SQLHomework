package com.competition.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Award {
    
    private Integer awardId;
    private Integer competitionId;
    private String awardName;
    private String awardLevel = "none";
    private BigDecimal awardPercentage; // 获奖总分比例，如0.3表示前30%获奖
    private Integer priority; // 优先级，数值越小优先级越高
    private String criteriaDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
