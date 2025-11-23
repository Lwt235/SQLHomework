package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Role {
    
    private Integer roleId;
    private String roleCode;
    private String roleName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
