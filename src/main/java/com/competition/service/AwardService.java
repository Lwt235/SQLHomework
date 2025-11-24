package com.competition.service;

import com.competition.dto.AwardResultWithDetailsDTO;
import com.competition.entity.Award;
import com.competition.entity.AwardResult;
import com.competition.entity.Competition;
import com.competition.entity.JudgeAssignment;
import com.competition.entity.Registration;
import com.competition.entity.Submission;
import com.competition.entity.Team;
import com.competition.entity.User;
import com.competition.mapper.AwardMapper;
import com.competition.mapper.AwardResultMapper;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.JudgeAssignmentMapper;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.SubmissionMapper;
import com.competition.mapper.TeamMapper;
import com.competition.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AwardService {

    @Autowired
    private AwardMapper awardMapper;

    @Autowired
    private AwardResultMapper awardResultMapper;
    
    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private SubmissionMapper submissionMapper;
    
    @Autowired
    private JudgeAssignmentMapper judgeAssignmentMapper;
    
    @Autowired
    private TeamMapper teamMapper;
    
    @Autowired
    private UserMapper userMapper;

    public List<Award> getAllAwards() {
        return awardMapper.findAll();
    }

    public Optional<Award> getAwardById(Integer id) {
        return Optional.ofNullable(awardMapper.findById(id));
    }

    public Award createAward(Award award) {
        awardMapper.insert(award);
        return award;
    }

    public Award updateAward(Integer id, Award awardDetails) {
        Award award = awardMapper.findById(id);
        if (award == null) {
            throw new RuntimeException("Award not found");
        }

        award.setAwardName(awardDetails.getAwardName());
        award.setAwardLevel(awardDetails.getAwardLevel());
        award.setAwardPercentage(awardDetails.getAwardPercentage());
        award.setPriority(awardDetails.getPriority());
        award.setCriteriaDescription(awardDetails.getCriteriaDescription());

        awardMapper.update(award);
        return award;
    }

    public List<Award> getAwardsByCompetition(Integer competitionId) {
        return awardMapper.findByCompetitionId(competitionId);
    }

    public AwardResult grantAward(Integer registrationId, Integer awardId, String certificateNo) {
        AwardResult result = new AwardResult();
        result.setRegistrationId(registrationId);
        result.setAwardId(awardId);
        result.setAwardTime(LocalDateTime.now());
        result.setCertificateNo(certificateNo);

        awardResultMapper.insert(result);
        return result;
    }

    public List<AwardResult> getAwardResultsByRegistration(Integer registrationId) {
        return awardResultMapper.findByRegistrationId(registrationId);
    }

    public List<AwardResult> getAwardResultsByAward(Integer awardId) {
        return awardResultMapper.findByAwardId(awardId);
    }
    
    /**
     * DTO class for holding registration score information
     */
    private static class RegistrationScore {
        Integer registrationId;
        BigDecimal averageScore;
        
        RegistrationScore(Integer registrationId, BigDecimal averageScore) {
            this.registrationId = registrationId;
            this.averageScore = averageScore;
        }
    }
    
    /**
     * Automatically distribute awards for a competition based on scores and award criteria
     * Awards are given based on percentage thresholds (e.g., top 30%)
     * When a registration qualifies for multiple awards, only the highest priority (smallest number) is kept
     * 
     * @param competitionId The competition ID to distribute awards for
     * @return Number of award results created
     */
    @Transactional
    public int autoDistributeAwards(Integer competitionId) {
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("竞赛不存在");
        }
        
        // Get all awards for this competition, sorted by priority (ascending)
        List<Award> awards = awardMapper.findByCompetitionId(competitionId).stream()
                .filter(award -> award.getAwardPercentage() != null && award.getPriority() != null)
                .sorted(Comparator.comparing(Award::getPriority))
                .collect(Collectors.toList());
        
        if (awards.isEmpty()) {
            throw new RuntimeException("该竞赛没有配置有效的奖项（需要设置获奖比例和优先级）");
        }
        
        // Get all registrations for this competition
        List<Registration> registrations = registrationMapper.findByCompetitionId(competitionId).stream()
                .filter(reg -> !reg.getDeleted() && "approved".equals(reg.getRegistrationStatus()))
                .collect(Collectors.toList());
        
        if (registrations.isEmpty()) {
            return 0;
        }
        
        // Calculate average scores for each registration
        List<RegistrationScore> registrationScores = new ArrayList<>();
        for (Registration registration : registrations) {
            BigDecimal avgScore = calculateRegistrationAverageScore(registration.getRegistrationId());
            if (avgScore != null) {
                registrationScores.add(new RegistrationScore(registration.getRegistrationId(), avgScore));
            }
        }
        
        if (registrationScores.isEmpty()) {
            throw new RuntimeException("没有可评分的报名记录");
        }
        
        // Sort by score descending (highest scores first)
        registrationScores.sort((a, b) -> b.averageScore.compareTo(a.averageScore));
        
        // Map to track which award each registration will receive (highest priority only)
        Map<Integer, Integer> registrationToAwardMap = new HashMap<>();
        
        // For each award, determine which registrations qualify
        for (Award award : awards) {
            int totalCount = registrationScores.size();
            int awardCount = (int) Math.ceil(totalCount * award.getAwardPercentage().doubleValue());
            
            // Get top N registrations based on percentage
            for (int i = 0; i < Math.min(awardCount, registrationScores.size()); i++) {
                Integer regId = registrationScores.get(i).registrationId;
                
                // Only assign if this registration doesn't have an award yet, or this award has higher priority
                if (!registrationToAwardMap.containsKey(regId)) {
                    registrationToAwardMap.put(regId, award.getAwardId());
                }
                // If already has an award, keep the one with higher priority (awards are sorted by priority)
            }
        }
        
        // Clear existing award results for this competition to avoid duplicates
        clearAwardResultsForCompetition(competitionId);
        
        // Create award results
        int createdCount = 0;
        for (Map.Entry<Integer, Integer> entry : registrationToAwardMap.entrySet()) {
            AwardResult result = new AwardResult();
            result.setRegistrationId(entry.getKey());
            result.setAwardId(entry.getValue());
            result.setAwardTime(LocalDateTime.now());
            
            awardResultMapper.insert(result);
            createdCount++;
        }
        
        return createdCount;
    }
    
    /**
     * Calculate the weighted average score for a registration based on all judge assignments
     * 
     * @param registrationId The registration ID
     * @return The weighted average score, or null if no scores are available
     */
    private BigDecimal calculateRegistrationAverageScore(Integer registrationId) {
        // Get the submission for this registration
        List<Submission> submissions = submissionMapper.findByRegistrationId(registrationId);
        if (submissions.isEmpty()) {
            return null;
        }
        
        // For simplicity, use the first submission (could be enhanced to handle multiple submissions)
        Submission submission = submissions.get(0);
        
        // Get all judge assignments for this submission
        List<JudgeAssignment> assignments = judgeAssignmentMapper.findBySubmissionId(submission.getSubmissionId()).stream()
                .filter(a -> a.getScore() != null && !a.getDeleted())
                .collect(Collectors.toList());
        
        if (assignments.isEmpty()) {
            return null;
        }
        
        // Calculate weighted average
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (JudgeAssignment assignment : assignments) {
            BigDecimal weight = assignment.getWeight() != null ? assignment.getWeight() : BigDecimal.ONE;
            totalWeightedScore = totalWeightedScore.add(assignment.getScore().multiply(weight));
            totalWeight = totalWeight.add(weight);
        }
        
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        return totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Clear all award results for a competition (used before redistributing awards)
     * 
     * @param competitionId The competition ID
     */
    private void clearAwardResultsForCompetition(Integer competitionId) {
        List<Award> awards = awardMapper.findByCompetitionId(competitionId);
        for (Award award : awards) {
            List<AwardResult> results = awardResultMapper.findByAwardId(award.getAwardId());
            for (AwardResult result : results) {
                awardResultMapper.deleteById(result.getRegistrationId(), result.getAwardId());
            }
        }
    }
    
    /**
     * Get all award results for a competition with detailed information (for publicity display)
     * 
     * @param competitionId The competition ID
     * @return List of award results with details for the competition
     */
    public List<AwardResultWithDetailsDTO> getAwardResultsByCompetitionWithDetails(Integer competitionId) {
        List<Award> awards = awardMapper.findByCompetitionId(competitionId);
        List<AwardResultWithDetailsDTO> allResults = new ArrayList<>();
        
        for (Award award : awards) {
            List<AwardResult> results = awardResultMapper.findByAwardId(award.getAwardId());
            for (AwardResult result : results) {
                AwardResultWithDetailsDTO dto = new AwardResultWithDetailsDTO();
                dto.setAwardResult(result);
                dto.setAward(award);
                
                // Load registration details
                Registration registration = registrationMapper.findById(result.getRegistrationId());
                dto.setRegistration(registration);
                
                if (registration != null) {
                    // Load team or user details
                    if (registration.getTeamId() != null) {
                        Team team = teamMapper.findById(registration.getTeamId());
                        dto.setTeam(team);
                    }
                    if (registration.getUserId() != null) {
                        User user = userMapper.findById(registration.getUserId());
                        dto.setUser(user);
                    }
                }
                
                allResults.add(dto);
            }
        }
        
        return allResults;
    }
    
    /**
     * Get all award results for a competition (simple version)
     * 
     * @param competitionId The competition ID
     * @return List of award results for the competition
     */
    public List<AwardResult> getAwardResultsByCompetition(Integer competitionId) {
        List<Award> awards = awardMapper.findByCompetitionId(competitionId);
        List<AwardResult> allResults = new ArrayList<>();
        
        for (Award award : awards) {
            List<AwardResult> results = awardResultMapper.findByAwardId(award.getAwardId());
            allResults.addAll(results);
        }
        
        return allResults;
    }
}
