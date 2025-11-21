package com.competition.service;

import com.competition.entity.Competition;
import com.competition.repository.CompetitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompetitionService {

    @Autowired
    private CompetitionRepository competitionRepository;

    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll().stream()
                .filter(competition -> !competition.getDeleted())
                .collect(Collectors.toList());
    }

    public Optional<Competition> getCompetitionById(Integer id) {
        return competitionRepository.findById(id);
    }

    public Competition createCompetition(Competition competition) {
        validateCompetitionDates(competition);
        return competitionRepository.save(competition);
    }

    public Competition updateCompetition(Integer id, Competition competitionDetails) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competition not found"));

        validateCompetitionDates(competitionDetails);
        
        competition.setCompetitionTitle(competitionDetails.getCompetitionTitle());
        competition.setShortTitle(competitionDetails.getShortTitle());
        competition.setCompetitionStatus(competitionDetails.getCompetitionStatus());
        competition.setDescription(competitionDetails.getDescription());
        competition.setCategory(competitionDetails.getCategory());
        competition.setLevel(competitionDetails.getLevel());
        competition.setOrganizer(competitionDetails.getOrganizer());
        competition.setStartDate(competitionDetails.getStartDate());
        competition.setEndDate(competitionDetails.getEndDate());
        competition.setSignupStart(competitionDetails.getSignupStart());
        competition.setSignupEnd(competitionDetails.getSignupEnd());
        competition.setSubmitStart(competitionDetails.getSubmitStart());
        competition.setSubmitEnd(competitionDetails.getSubmitEnd());
        competition.setReviewStart(competitionDetails.getReviewStart());
        competition.setReviewEnd(competitionDetails.getReviewEnd());
        competition.setAwardPublishDate(competitionDetails.getAwardPublishDate());
        competition.setMaxTeamSize(competitionDetails.getMaxTeamSize());

        return competitionRepository.save(competition);
    }

    public void deleteCompetition(Integer id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
        competition.setDeleted(true);
        competitionRepository.save(competition);
    }

    public List<Competition> getCompetitionsByStatus(String status) {
        return competitionRepository.findByCompetitionStatus(status).stream()
                .filter(competition -> !competition.getDeleted())
                .collect(Collectors.toList());
    }
    
    private void validateCompetitionDates(Competition competition) {
        // Validate start and end dates
        if (competition.getStartDate() != null && competition.getEndDate() != null) {
            if (competition.getStartDate().isAfter(competition.getEndDate())) {
                throw new RuntimeException("比赛开始时间必须早于结束时间");
            }
        }
        
        // Validate signup dates
        if (competition.getSignupStart() != null && competition.getSignupEnd() != null) {
            if (competition.getSignupStart().isAfter(competition.getSignupEnd())) {
                throw new RuntimeException("报名开始时间必须早于报名结束时间");
            }
        }
        
        // Validate submit dates
        if (competition.getSubmitStart() != null && competition.getSubmitEnd() != null) {
            if (competition.getSubmitStart().isAfter(competition.getSubmitEnd())) {
                throw new RuntimeException("提交开始时间必须早于提交结束时间");
            }
        }
        
        // Validate review dates
        if (competition.getReviewStart() != null && competition.getReviewEnd() != null) {
            if (competition.getReviewStart().isAfter(competition.getReviewEnd())) {
                throw new RuntimeException("评审开始时间必须早于评审结束时间");
            }
        }
        
        // Validate that signup ends before competition starts
        if (competition.getSignupEnd() != null && competition.getStartDate() != null) {
            if (competition.getSignupEnd().isAfter(competition.getStartDate())) {
                throw new RuntimeException("报名结束时间应该在比赛开始时间之前");
            }
        }
        
        // Validate that submission period is within competition period
        if (competition.getSubmitStart() != null && competition.getStartDate() != null) {
            if (competition.getSubmitStart().isBefore(competition.getStartDate())) {
                throw new RuntimeException("提交开始时间不应早于比赛开始时间");
            }
        }
        
        if (competition.getSubmitEnd() != null && competition.getEndDate() != null) {
            if (competition.getSubmitEnd().isAfter(competition.getEndDate())) {
                throw new RuntimeException("提交结束时间不应晚于比赛结束时间");
            }
        }
        
        // Validate that review period comes after submission period
        if (competition.getReviewStart() != null && competition.getSubmitEnd() != null) {
            if (competition.getReviewStart().isBefore(competition.getSubmitEnd())) {
                throw new RuntimeException("评审开始时间应该在提交结束时间之后");
            }
        }
    }
}
