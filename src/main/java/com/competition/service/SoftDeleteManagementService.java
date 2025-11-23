package com.competition.service;

import com.competition.entity.*;
import com.competition.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SoftDeleteManagementService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private AwardMapper awardMapper;

    /**
     * Get all soft-deleted entities summary
     */
    public Map<String, Integer> getSoftDeletedSummary() {
        Map<String, Integer> summary = new HashMap<>();
        
        summary.put("users", (int) userMapper.findAll().stream()
                .filter(User::getDeleted)
                .count());
        
        summary.put("competitions", (int) competitionMapper.findAll().stream()
                .filter(Competition::getDeleted)
                .count());
        
        summary.put("registrations", (int) registrationMapper.findAll().stream()
                .filter(Registration::getDeleted)
                .count());
        
        summary.put("submissions", (int) submissionMapper.findAll().stream()
                .filter(Submission::getDeleted)
                .count());
        
        summary.put("awards", (int) awardMapper.findAll().stream()
                .filter(Award::getDeleted)
                .count());
        
        return summary;
    }

    /**
     * Get all soft-deleted users
     */
    public List<User> getSoftDeletedUsers() {
        return userMapper.findAll().stream()
                .filter(User::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Get all soft-deleted competitions
     */
    public List<Competition> getSoftDeletedCompetitions() {
        return competitionMapper.findAll().stream()
                .filter(Competition::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Get all soft-deleted registrations
     */
    public List<Registration> getSoftDeletedRegistrations() {
        return registrationMapper.findAll().stream()
                .filter(Registration::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Get all soft-deleted submissions
     */
    public List<Submission> getSoftDeletedSubmissions() {
        return submissionMapper.findAll().stream()
                .filter(Submission::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Get all soft-deleted awards
     */
    public List<Award> getSoftDeletedAwards() {
        return awardMapper.findAll().stream()
                .filter(Award::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Permanently delete a user
     */
    @Transactional
    public void permanentlyDeleteUser(Integer userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的用户");
        }
        
        userMapper.delete(user);
    }

    /**
     * Permanently delete multiple users
     */
    @Transactional
    public void permanentlyDeleteUsers(List<Integer> userIds) {
        for (Integer userId : userIds) {
            permanentlyDeleteUser(userId);
        }
    }

    /**
     * Permanently delete a competition
     */
    @Transactional
    public void permanentlyDeleteCompetition(Integer competitionId) {
        Competition competition = competitionMapper.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
        
        if (!competition.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的比赛");
        }
        
        competitionMapper.delete(competition);
    }

    /**
     * Permanently delete multiple competitions
     */
    @Transactional
    public void permanentlyDeleteCompetitions(List<Integer> competitionIds) {
        for (Integer competitionId : competitionIds) {
            permanentlyDeleteCompetition(competitionId);
        }
    }

    /**
     * Permanently delete a registration
     */
    @Transactional
    public void permanentlyDeleteRegistration(Integer registrationId) {
        Registration registration = registrationMapper.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        
        if (!registration.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的报名记录");
        }
        
        registrationMapper.delete(registration);
    }

    /**
     * Permanently delete multiple registrations
     */
    @Transactional
    public void permanentlyDeleteRegistrations(List<Integer> registrationIds) {
        for (Integer registrationId : registrationIds) {
            permanentlyDeleteRegistration(registrationId);
        }
    }

    /**
     * Permanently delete a submission
     */
    @Transactional
    public void permanentlyDeleteSubmission(Integer submissionId) {
        Submission submission = submissionMapper.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        if (!submission.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的提交记录");
        }
        
        submissionMapper.delete(submission);
    }

    /**
     * Permanently delete multiple submissions
     */
    @Transactional
    public void permanentlyDeleteSubmissions(List<Integer> submissionIds) {
        for (Integer submissionId : submissionIds) {
            permanentlyDeleteSubmission(submissionId);
        }
    }

    /**
     * Permanently delete an award
     */
    @Transactional
    public void permanentlyDeleteAward(Integer awardId) {
        Award award = awardMapper.findById(awardId)
                .orElseThrow(() -> new RuntimeException("Award not found"));
        
        if (!award.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的奖项");
        }
        
        awardMapper.delete(award);
    }

    /**
     * Permanently delete multiple awards
     */
    @Transactional
    public void permanentlyDeleteAwards(List<Integer> awardIds) {
        for (Integer awardId : awardIds) {
            permanentlyDeleteAward(awardId);
        }
    }
}
