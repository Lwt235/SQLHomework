package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "User")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "user_status", nullable = false, length = 20)
    private String userStatus = "inactive";
    
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 256)
    private String passwordHash;
    
    @Column(name = "real_name", length = 100)
    private String realName;
    
    @Column(name = "nickname", length = 100)
    private String nickname;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "school", length = 256)
    private String school;
    
    @Column(name = "department", length = 256)
    private String department;
    
    @Column(name = "student_no", length = 50)
    private String studentNo;
    
    @Column(name = "auth_type", nullable = false, length = 20)
    private String authType = "local";
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
