package com.competition.dto;

import com.competition.entity.Award;
import com.competition.entity.AwardResult;
import com.competition.entity.Registration;
import com.competition.entity.Team;
import com.competition.entity.User;
import lombok.Data;

/**
 * DTO for award results with detailed information
 */
@Data
public class AwardResultWithDetailsDTO {
    private AwardResult awardResult;
    private Award award;
    private Registration registration;
    private Team team;
    private User user;
}
