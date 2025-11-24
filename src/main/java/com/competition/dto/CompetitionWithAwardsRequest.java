package com.competition.dto;

import com.competition.entity.Award;
import com.competition.entity.Competition;
import lombok.Data;
import java.util.List;

/**
 * DTO for creating a competition with awards
 */
@Data
public class CompetitionWithAwardsRequest {
    private Competition competition;
    private List<Award> awards;
}
