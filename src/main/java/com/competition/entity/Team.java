package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Team {
    
    private Integer teamId;
    private String teamName;
    private LocalDateTime formedAt;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
