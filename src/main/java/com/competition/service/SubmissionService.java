package com.competition.service;

import com.competition.entity.Submission;
import com.competition.entity.Registration;
import com.competition.entity.TeamMember;
import com.competition.entity.JudgeAssignment;
import com.competition.repository.SubmissionRepository;
import com.competition.repository.RegistrationRepository;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.JudgeAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private JudgeAssignmentRepository judgeAssignmentRepository;

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public Optional<Submission> getSubmissionById(Integer id) {
        return submissionRepository.findById(id);
    }

    public Submission createSubmission(Submission submission) {
        // Check if registration already has a locked submission
        List<Submission> existingSubmissions = submissionRepository.findByRegistrationId(submission.getRegistrationId());
        boolean hasLockedSubmission = existingSubmissions.stream()
                .anyMatch(s -> s.getFinalLockedAt() != null || "locked".equals(s.getSubmissionStatus()));
        
        if (hasLockedSubmission) {
            throw new RuntimeException("该报名已有锁定的作品，无法创建新作品");
        }
        
        submission.setSubmissionStatus("draft");
        return submissionRepository.save(submission);
    }

    public Submission updateSubmission(Integer id, Submission submissionDetails) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Check if submission is locked - if so, prevent editing
        if (submission.getFinalLockedAt() != null) {
            throw new RuntimeException("作品已最终锁定，无法再修改");
        }

        submission.setSubmissionTitle(submissionDetails.getSubmissionTitle());
        submission.setAbstractText(submissionDetails.getAbstractText());
        submission.setSubmissionStatus(submissionDetails.getSubmissionStatus());

        return submissionRepository.save(submission);
    }

    public Submission submitWork(Integer id, Integer userId) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Check if submission is locked
        if (submission.getFinalLockedAt() != null) {
            throw new RuntimeException("作品已最终锁定，无法再修改");
        }
        
        // Check if user has permission (must be team leader for team registrations)
        if (!canUserModifySubmission(submission.getRegistrationId(), userId)) {
            throw new RuntimeException("只有队长可以提交团队作品");
        }

        submission.setSubmissionStatus("submitted");
        submission.setSubmittedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission lockSubmission(Integer id, Integer userId) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Check if already locked
        if (submission.getFinalLockedAt() != null) {
            throw new RuntimeException("作品已经最终锁定");
        }
        
        // Check if user has permission (must be team leader for team registrations)
        if (!canUserModifySubmission(submission.getRegistrationId(), userId)) {
            throw new RuntimeException("只有队长可以锁定团队作品");
        }

        // Set final lock time
        submission.setFinalLockedAt(LocalDateTime.now());
        submission.setSubmissionStatus("locked");
        
        // Save the locked submission
        Submission lockedSubmission = submissionRepository.save(submission);
        
        // Set other submissions for this registration as invalid
        List<Submission> otherSubmissions = submissionRepository.findByRegistrationId(submission.getRegistrationId());
        for (Submission other : otherSubmissions) {
            if (!other.getSubmissionId().equals(id) && other.getFinalLockedAt() == null) {
                other.setSubmissionStatus("invalid");
                submissionRepository.save(other);
            }
        }

        return lockedSubmission;
    }
    
    /**
     * Check if user can modify submission (create, edit, submit, lock)
     * For individual registration: user must be the registrant
     * For team registration: user must be the team leader
     */
    public boolean canUserModifySubmission(Integer registrationId, Integer userId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        
        // Individual registration
        if (registration.getUserId() != null && registration.getTeamId() == null) {
            return registration.getUserId().equals(userId);
        }
        
        // Team registration - check if user is team leader
        if (registration.getTeamId() != null) {
            TeamMember.TeamMemberId memberId = new TeamMember.TeamMemberId();
            memberId.setTeamId(registration.getTeamId());
            memberId.setUserId(userId);
            
            Optional<TeamMember> memberOpt = teamMemberRepository.findById(memberId);
            if (memberOpt.isPresent()) {
                return TeamService.ROLE_LEADER.equals(memberOpt.get().getRoleInTeam());
            }
        }
        
        return false;
    }
    
    /**
     * Check if user can view submission
     * For individual registration: user must be the registrant
     * For team registration: user must be a team member
     */
    public boolean canUserViewSubmission(Integer registrationId, Integer userId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        
        // Individual registration
        if (registration.getUserId() != null && registration.getTeamId() == null) {
            return registration.getUserId().equals(userId);
        }
        
        // Team registration - check if user is any team member
        if (registration.getTeamId() != null) {
            TeamMember.TeamMemberId memberId = new TeamMember.TeamMemberId();
            memberId.setTeamId(registration.getTeamId());
            memberId.setUserId(userId);
            
            return teamMemberRepository.findById(memberId).isPresent();
        }
        
        return false;
    }

    public List<Submission> getSubmissionsByRegistration(Integer registrationId) {
        return submissionRepository.findByRegistrationId(registrationId);
    }
}
