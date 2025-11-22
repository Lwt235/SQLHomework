package com.competition.dto;

import com.competition.entity.Competition;
import com.competition.entity.Registration;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationWithCompetitionDTO {
    private Integer registrationId;
    private Integer competitionId;
    private Integer teamId;
    private Integer userId;
    private Integer auditUserId;
    private String registrationStatus;
    private LocalDateTime auditTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
    
    // Competition details
    private Competition competition;
    
    public static RegistrationWithCompetitionDTO from(Registration registration, Competition competition) {
        RegistrationWithCompetitionDTO dto = new RegistrationWithCompetitionDTO();
        dto.setRegistrationId(registration.getRegistrationId());
        dto.setCompetitionId(registration.getCompetitionId());
        dto.setTeamId(registration.getTeamId());
        dto.setUserId(registration.getUserId());
        dto.setAuditUserId(registration.getAuditUserId());
        dto.setRegistrationStatus(registration.getRegistrationStatus());
        dto.setAuditTime(registration.getAuditTime());
        dto.setRemark(registration.getRemark());
        dto.setCreatedAt(registration.getCreatedAt());
        dto.setUpdatedAt(registration.getUpdatedAt());
        dto.setDeleted(registration.getDeleted());
        dto.setCompetition(competition);
        return dto;
    }
}
