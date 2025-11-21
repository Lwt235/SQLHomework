package com.competition.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "TeamMember")
@IdClass(TeamMember.TeamMemberId.class)
public class TeamMember {
    
    @Id
    @Column(name = "user_id")
    private Integer userId;
    
    @Id
    @Column(name = "team_id")
    private Integer teamId;
    
    @Column(name = "role_in_team", nullable = false, length = 20)
    private String roleInTeam = "member";
    
    @Data
    public static class TeamMemberId implements Serializable {
        private Integer userId;
        private Integer teamId;
    }
}
