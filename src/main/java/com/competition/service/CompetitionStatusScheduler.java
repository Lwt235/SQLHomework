package com.competition.service;

import com.competition.entity.Competition;
import com.competition.mapper.CompetitionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to automatically update competition status based on time
 */
@Service
public class CompetitionStatusScheduler {
    
    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private TimeService timeService;
    
    /**
     * Update competition statuses based on current time
     * Configurable interval via application.properties
     */
    @Scheduled(fixedRateString = "${competition.status.update.interval:3600000}")
    @Transactional
    public void updateCompetitionStatuses() {
        LocalDateTime now = timeService.getCurrentTime();
        
        // Only load competitions that might need status updates (not already finished)
        List<Competition> competitions = competitionMapper.findAll().stream()
                .filter(c -> !c.getDeleted() && !"finished".equals(c.getCompetitionStatus()))
                .collect(Collectors.toList());
        
        for (Competition competition : competitions) {
            String newStatus = determineCompetitionStatus(competition, now);
            
            // Only update if status changed
            if (newStatus != null && !newStatus.equals(competition.getCompetitionStatus())) {
                competition.setCompetitionStatus(newStatus);
                competitionMapper.update(competition);
            }
        }
    }
    
    /**
     * Manually trigger competition status update
     * This method can be called by administrators to immediately refresh competition statuses
     */
    @Transactional
    public void manualUpdateCompetitionStatuses() {
        updateCompetitionStatuses();
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
