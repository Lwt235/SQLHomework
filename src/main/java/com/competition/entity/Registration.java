package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Registration")
public class Registration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Integer registrationId;
    
    @Column(name = "competition_id", nullable = false)
    private Integer competitionId;
    
    @Column(name = "team_id")
    private Integer teamId;
    
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "team_name", length = 100)
    private String teamName;
    
    @Column(name = "audit_user_id")
    private Integer auditUserId;
    
    @Column(name = "registration_status", nullable = false, length = 20)
    private String registrationStatus = "pending";
    
    @Column(name = "audit_time")
    private LocalDateTime auditTime;
    
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
    
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
