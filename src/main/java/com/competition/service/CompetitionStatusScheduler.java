package com.competition.service;

import com.competition.entity.Competition;
import com.competition.repository.CompetitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to automatically update competition status based on time
 */
@Service
public class CompetitionStatusScheduler {
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Autowired
    private TimeService timeService;
    
    /**
     * Update competition statuses based on current time
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void updateCompetitionStatuses() {
        LocalDateTime now = timeService.getCurrentTime();
        
        List<Competition> competitions = competitionRepository.findAll();
        
        for (Competition competition : competitions) {
            if (competition.getDeleted()) {
                continue;
            }
            
            String newStatus = determineCompetitionStatus(competition, now);
            
            // Only update if status changed
            if (newStatus != null && !newStatus.equals(competition.getCompetitionStatus())) {
                competition.setCompetitionStatus(newStatus);
                competitionRepository.save(competition);
            }
        }
    }
    
    /**
     * Determine competition status based on time
     */
    private String determineCompetitionStatus(Competition competition, LocalDateTime now) {
        // If manually set to draft, don't auto-update
        if ("draft".equals(competition.getCompetitionStatus())) {
            // Check if should be published (signup has started)
            if (competition.getSignupStart() != null && now.isAfter(competition.getSignupStart())) {
                return "published";
            }
            return null; // Keep as draft
        }
        
        // Check if finished (past end date or past award publish date)
        LocalDateTime endDate = competition.getEndDate();
        LocalDateTime awardPublishDate = competition.getAwardPublishDate();
        
        if (awardPublishDate != null && now.isAfter(awardPublishDate)) {
            return "finished";
        }
        
        if (endDate != null && now.isAfter(endDate)) {
            return "finished";
        }
        
        // Check if ongoing (past start date)
        LocalDateTime startDate = competition.getStartDate();
        if (startDate != null && now.isAfter(startDate)) {
            return "ongoing";
        }
        
        // Check if published (past signup start)
        LocalDateTime signupStart = competition.getSignupStart();
        if (signupStart != null && now.isAfter(signupStart)) {
            return "published";
        }
        
        return null; // No change
    }
}
