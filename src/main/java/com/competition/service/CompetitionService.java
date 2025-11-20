package com.competition.service;

import com.competition.entity.Competition;
import com.competition.repository.CompetitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CompetitionService {

    @Autowired
    private CompetitionRepository competitionRepository;

    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll();
    }

    public Optional<Competition> getCompetitionById(Integer id) {
        return competitionRepository.findById(id);
    }

    public Competition createCompetition(Competition competition) {
        return competitionRepository.save(competition);
    }

    public Competition updateCompetition(Integer id, Competition competitionDetails) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competition not found"));

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
        competition.setIsDeleted(true);
        competitionRepository.save(competition);
    }

    public List<Competition> getCompetitionsByStatus(String status) {
        return competitionRepository.findByCompetitionStatus(status);
    }
}
