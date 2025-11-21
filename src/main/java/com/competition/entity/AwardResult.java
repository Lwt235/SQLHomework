package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
@Entity
@Table(name = "AwardResult")
@IdClass(AwardResult.AwardResultId.class)
public class AwardResult {
    
    @Id
    @Column(name = "registration_id")
    private Integer registrationId;
    
    @Id
    @Column(name = "award_id")
    private Integer awardId;
    
    @Column(name = "award_time", nullable = false)
    private LocalDateTime awardTime;
    
    @Column(name = "certificate_no", length = 100)
    private String certificateNo;
    
    @Column(name = "certificate_path", length = 500)
    private String certificatePath;
    
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
    
    @Data
    public static class AwardResultId implements Serializable {
        private Integer registrationId;
        private Integer awardId;
    }
}
