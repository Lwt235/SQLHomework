package com.competition.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileEntity {
    
    private Integer fileId;
    private Integer uploadUserId;
    private String storageKey;
    private String fileName;
    private String fileType = "other";
    private Integer sizeBytes;
    private String checksum;
    private String versionTag;
    private LocalDateTime createdAt;
    private Boolean deleted = false;
}
