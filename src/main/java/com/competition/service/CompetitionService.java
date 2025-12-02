package com.competition.service;

import com.competition.entity.Award;
import com.competition.entity.Competition;
import com.competition.mapper.AwardMapper;
import com.competition.mapper.CompetitionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompetitionService {

    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private AwardMapper awardMapper;

    public List<Competition> getAllCompetitions() {
        return competitionMapper.findAll().stream()
                .filter(competition -> !competition.getDeleted())
                .collect(Collectors.toList());
    }

    public Optional<Competition> getCompetitionById(Integer id) {
        return Optional.ofNullable(competitionMapper.findById(id));
    }

    public Competition createCompetition(Competition competition) {
        validateCompetitionDates(competition);
        competitionMapper.insert(competition);
        return competition;
    }
    
    @Transactional
    public Competition createCompetitionWithAwards(Competition competition, List<Award> awards) {
        // First create the competition
        validateCompetitionDates(competition);
        competitionMapper.insert(competition);
        
        // Then create the awards if provided
        if (awards != null && !awards.isEmpty()) {
            for (Award award : awards) {
                award.setCompetitionId(competition.getCompetitionId());
                awardMapper.insert(award);
            }
        }
        
        return competition;
    }

    public Competition updateCompetition(Integer id, Competition competitionDetails) {
        Competition competition = competitionMapper.findById(id);
        if (competition == null) {
            throw new RuntimeException("Competition not found");
        }

        validateCompetitionDates(competitionDetails);
        
        competition.setCompetitionTitle(competitionDetails.getCompetitionTitle());
        competition.setShortTitle(competitionDetails.getShortTitle());
        competition.setCompetitionStatus(competitionDetails.getCompetitionStatus());
        competition.setDescription(competitionDetails.getDescription());
        competition.setCategory(competitionDetails.getCategory());
        competition.setLevel(competitionDetails.getLevel());
        competition.setOrganizer(competitionDetails.getOrganizer());
        competition.setSignupStart(competitionDetails.getSignupStart());
        competition.setSubmitStart(competitionDetails.getSubmitStart());
        competition.setReviewStart(competitionDetails.getReviewStart());
        competition.setAwardPublishStart(competitionDetails.getAwardPublishStart());
        competition.setMaxTeamSize(competitionDetails.getMaxTeamSize());

        competitionMapper.update(competition);
        return competition;
    }

    public void deleteCompetition(Integer id) {
        Competition competition = competitionMapper.findById(id);
        if (competition == null) {
            throw new RuntimeException("Competition not found");
        }
        competition.setDeleted(true);
        competitionMapper.update(competition);
    }

    public List<Competition> getCompetitionsByStatus(String status) {
        return competitionMapper.findByCompetitionStatus(status).stream()
                .filter(competition -> !competition.getDeleted())
                .collect(Collectors.toList());
    }
    
    private void validateCompetitionDates(Competition competition) {
        // Validate signup start and submit start (which is also signup end)
        if (competition.getSignupStart() != null && competition.getSubmitStart() != null) {
            if (!competition.getSignupStart().isBefore(competition.getSubmitStart())) {
                throw new RuntimeException("报名开始时间必须早于作品提交开始时间");
            }
        }
        
        // Validate submit start and review start
        if (competition.getSubmitStart() != null && competition.getReviewStart() != null) {
            if (!competition.getSubmitStart().isBefore(competition.getReviewStart())) {
                throw new RuntimeException("作品提交开始时间必须早于评审开始时间");
            }
        }
        
        // Validate review start and award publish start
        if (competition.getReviewStart() != null && competition.getAwardPublishStart() != null) {
            if (!competition.getReviewStart().isBefore(competition.getAwardPublishStart())) {
                throw new RuntimeException("评审开始时间必须早于奖项公示时间");
            }
        }
        
        // Validate submit start and award publish start (in case review_start is not provided)
        if (competition.getSubmitStart() != null && competition.getAwardPublishStart() != null) {
            if (!competition.getSubmitStart().isBefore(competition.getAwardPublishStart())) {
                throw new RuntimeException("作品提交开始时间必须早于奖项公示时间");
            }
        }
    }
}
