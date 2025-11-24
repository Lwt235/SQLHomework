package com.competition.service;

import com.competition.entity.Competition;
import com.competition.mapper.CompetitionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(CompetitionStatusScheduler.class);
    
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
        
        // Load all active competitions (including finished ones, as they may need to be updated)
        List<Competition> competitions = competitionMapper.findAll().stream()
                .filter(c -> !c.getDeleted())
                .collect(Collectors.toList());
        
        int updatedCount = 0;
        for (Competition competition : competitions) {
            String newStatus = determineCompetitionStatus(competition, now);
            
            // Only update if status changed
            if (newStatus != null && !newStatus.equals(competition.getCompetitionStatus())) {
                logger.info("Updating competition {} status from {} to {}", 
                    competition.getCompetitionId(), competition.getCompetitionStatus(), newStatus);
                String oldStatus = competition.getCompetitionStatus();
                competition.setCompetitionStatus(newStatus);
                competitionMapper.update(competition);
                updatedCount++;
                logger.info("Successfully updated competition {} status from {} to {} in database", 
                    competition.getCompetitionId(), oldStatus, newStatus);
            }
        }
        logger.info("Competition status update completed. {} competitions updated.", updatedCount);
    }
    
    /**
     * Manually trigger competition status update
     * This method can be called by administrators to immediately refresh competition statuses
     */
    @Transactional
    public void manualUpdateCompetitionStatuses() {
        logger.info("Manual competition status update triggered by administrator");
        updateCompetitionStatuses();
        logger.info("Manual competition status update completed");
    }
    
    /**
     * Determine competition status based on time
     * New granular statuses:
     * - draft: 草稿 (not yet published)
     * - registering: 报名中 (signup period)
     * - submitting: 进行中-提交作品 (submission period)
     * - reviewing: 评审中 (review period)
     * - publicizing: 公示中 (award publication period)
     * - finished: 已结束 (completed)
     */
    private String determineCompetitionStatus(Competition competition, LocalDateTime now) {
        // If manually set to draft, don't auto-update unless signup has started
        if ("draft".equals(competition.getCompetitionStatus())) {
            // Check if should be published (signup has started)
            if (competition.getSignupStart() != null && now.isAfter(competition.getSignupStart())) {
                return "registering";
            }
            return null; // Keep as draft
        }
        
        // Check completion status first (from end to beginning)
        LocalDateTime endDate = competition.getEndDate();
        if (endDate != null && now.isAfter(endDate)) {
            return "finished";
        }
        
        // Check if in award publication period (公示中)
        LocalDateTime awardPublishStart = competition.getAwardPublishStart();
        if (awardPublishStart != null && now.isAfter(awardPublishStart)) {
            // Still in publication period (before endDate)
            return "publicizing";
        }
        
        // Check if in review period (评审中)
        LocalDateTime reviewStart = competition.getReviewStart();
        if (reviewStart != null && now.isAfter(reviewStart)) {
            // In review period (after review start, before award publish)
            return "reviewing";
        }
        
        // Check if in submission period (进行中-提交作品)
        LocalDateTime submitStart = competition.getSubmitStart();
        if (submitStart != null && now.isAfter(submitStart)) {
            // In submission period (after submit start, before review start or award publish)
            return "submitting";
        }
        
        // Check if in signup period (报名中)
        LocalDateTime signupStart = competition.getSignupStart();
        if (signupStart != null && now.isAfter(signupStart)) {
            // In signup period (after signup start, before submit start)
            return "registering";
        }
        
        return null; // No change
    }
}
