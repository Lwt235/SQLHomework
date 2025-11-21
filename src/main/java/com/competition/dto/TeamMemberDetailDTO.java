package com.competition.dto;

import lombok.Data;

@Data
public class TeamMemberDetailDTO {
    private Integer userId;
    private Integer teamId;
    private String roleInTeam;
    private String username;
    private String nickname;
    private String realName;
    private Boolean isCurrentUser;
}
