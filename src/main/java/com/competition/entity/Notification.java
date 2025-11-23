package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    
    private Integer notificationId;
    private Integer userId;
    private String notificationType;
    private String title;
    private String content;
    private Boolean read = false;
    private Integer relatedId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
