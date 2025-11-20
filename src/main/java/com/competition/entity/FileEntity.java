package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "File")
public class FileEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer fileId;
    
    @Column(name = "upload_user_id", nullable = false)
    private Integer uploadUserId;
    
    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_type", nullable = false, length = 32)
    private String fileType = "other";
    
    @Column(name = "size_bytes")
    private Integer sizeBytes;
    
    @Column(name = "checksum", length = 128)
    private String checksum;
    
    @Column(name = "version_tag", length = 50)
    private String versionTag;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
