package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    
    private Integer userId;
    private String userStatus = "inactive";
    private String username;
    private String passwordHash;
    private String realName;
    private String email;
    private String phone;
    private String school;
    private String department;
    private String studentNo;
    private String authType = "local";
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
