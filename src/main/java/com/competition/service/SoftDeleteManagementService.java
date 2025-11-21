package com.competition.service;

import com.competition.entity.*;
import com.competition.repository.*;
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
    private UserRepository userRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AwardRepository awardRepository;

    /**
     * Get all soft-deleted entities summary
     */
    public Map<String, Integer> getSoftDeletedSummary() {
        Map<String, Integer> summary = new HashMap<>();
        
        summary.put("users", (int) userRepository.findAll().stream()
                .filter(User::getDeleted)
                .count());
        
        summary.put("competitions", (int) competitionRepository.findAll().stream()
                .filter(Competition::getDeleted)
                .count());
        
        summary.put("registrations", (int) registrationRepository.findAll().stream()
                .filter(Registration::getDeleted)
                .count());
        
        summary.put("submissions", (int) submissionRepository.findAll().stream()
                .filter(Submission::getDeleted)
                .count());
        
        summary.put("awards", (int) awardRepository.findAll().stream()
                .filter(Award::getDeleted)
                .count());
        
        return summary;
    }

    /**
     * Get all soft-deleted users
     */
    public List<User> getSoftDeletedUsers() {
        return userRepository.findAll().stream()
                .filter(User::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Get all soft-deleted competitions
     */
    public List<Competition> getSoftDeletedCompetitions() {
        return competitionRepository.findAll().stream()
                .filter(Competition::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Get all soft-deleted registrations
     */
    public List<Registration> getSoftDeletedRegistrations() {
        return registrationRepository.findAll().stream()
                .filter(Registration::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Get all soft-deleted submissions
     */
    public List<Submission> getSoftDeletedSubmissions() {
        return submissionRepository.findAll().stream()
                .filter(Submission::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Get all soft-deleted awards
     */
    public List<Award> getSoftDeletedAwards() {
        return awardRepository.findAll().stream()
                .filter(Award::getDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Permanently delete a user
     */
    @Transactional
    public void permanentlyDeleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的用户");
        }
        
        userRepository.delete(user);
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
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
        
        if (!competition.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的比赛");
        }
        
        competitionRepository.delete(competition);
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
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        
        if (!registration.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的报名记录");
        }
        
        registrationRepository.delete(registration);
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
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        if (!submission.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的提交记录");
        }
        
        submissionRepository.delete(submission);
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
        Award award = awardRepository.findById(awardId)
                .orElseThrow(() -> new RuntimeException("Award not found"));
        
        if (!award.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的奖项");
        }
        
        awardRepository.delete(award);
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
