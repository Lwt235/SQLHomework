package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "UserRole")
@IdClass(UserRole.UserRoleId.class)
public class UserRole {
    
    @Id
    @Column(name = "user_id")
    private Integer userId;
    
    @Id
    @Column(name = "role_id")
    private Integer roleId;
    
    @Data
    public static class UserRoleId implements Serializable {
        private Integer userId;
        private Integer roleId;
    }
}
