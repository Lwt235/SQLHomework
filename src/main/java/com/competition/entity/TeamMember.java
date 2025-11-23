package com.competition.entity;

import lombok.Data;

@Data
public class TeamMember {
    
    private Integer userId;
    private Integer teamId;
    private String roleInTeam = "member";
}
