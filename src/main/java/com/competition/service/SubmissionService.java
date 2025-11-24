package com.competition.service;

import com.competition.entity.Submission;
import com.competition.entity.Registration;
import com.competition.entity.Competition;
import com.competition.entity.TeamMember;
import com.competition.entity.JudgeAssignment;
import com.competition.mapper.SubmissionMapper;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.TeamMemberMapper;
import com.competition.mapper.JudgeAssignmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionMapper submissionMapper;
    
    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private TeamMemberMapper teamMemberMapper;
    
    @Autowired
    private JudgeAssignmentMapper judgeAssignmentMapper;

    public List<Submission> getAllSubmissions() {
        return submissionMapper.findAll();
    }

    public Optional<Submission> getSubmissionById(Integer id) {
        return Optional.ofNullable(submissionMapper.findById(id));
    }

    public Submission createSubmission(Submission submission) {
        // Check if registration already has a locked submission
        List<Submission> existingSubmissions = submissionMapper.findByRegistrationId(submission.getRegistrationId());
        boolean hasLockedSubmission = existingSubmissions.stream()
                .anyMatch(s -> s.getFinalLockedAt() != null || "locked".equals(s.getSubmissionStatus()));
        
        if (hasLockedSubmission) {
            throw new RuntimeException("该报名已有锁定的作品，无法创建新作品");
        }
        
        submission.setSubmissionStatus("draft");
        submissionMapper.insert(submission);
        return submission;
    }

    public Submission updateSubmission(Integer id, Submission submissionDetails) {
        Submission submission = submissionMapper.findById(id);
        if (submission == null) {
            throw new RuntimeException("Submission not found");
        }

        // Check if submission is locked - if so, prevent editing
        if (submission.getFinalLockedAt() != null) {
            throw new RuntimeException("作品已最终锁定，无法再修改");
        }

        submission.setSubmissionTitle(submissionDetails.getSubmissionTitle());
        submission.setAbstractText(submissionDetails.getAbstractText());
        submission.setSubmissionStatus(submissionDetails.getSubmissionStatus());

        submissionMapper.update(submission);
        return submission;
    }

    public Submission submitWork(Integer id, Integer userId) {
        Submission submission = submissionMapper.findById(id);
        if (submission == null) {
            throw new RuntimeException("Submission not found");
        }

        // Check if submission is locked
        if (submission.getFinalLockedAt() != null) {
            throw new RuntimeException("作品已最终锁定，无法再修改");
        }
        
        // Check if user has permission (must be team leader for team registrations)
        if (!canUserModifySubmission(submission.getRegistrationId(), userId)) {
            throw new RuntimeException("只有队长可以提交团队作品");
        }
        
        // Check if competition submission period is valid
        Registration registration = registrationMapper.findById(submission.getRegistrationId());
        if (registration == null) {
            throw new RuntimeException("报名信息不存在");
        }
        
        Competition competition = competitionMapper.findById(registration.getCompetitionId());
        if (competition == null) {
            throw new RuntimeException("竞赛信息不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Check if submission period has started
        if (competition.getSubmitStart() != null && now.isBefore(competition.getSubmitStart())) {
            throw new RuntimeException("竞赛作品提交期尚未开始");
        }
        
        // Check if submission period has ended
        if (competition.getAwardPublishStart() != null && now.isAfter(competition.getAwardPublishStart())) {
            throw new RuntimeException("竞赛作品提交期已结束");
        }

        submission.setSubmissionStatus("submitted");
        submission.setSubmittedAt(LocalDateTime.now());

        submissionMapper.update(submission);
        return submission;
    }

    @Transactional
    public Submission lockSubmission(Integer id, Integer userId) {
        Submission submission = submissionMapper.findById(id);
        if (submission == null) {
            throw new RuntimeException("Submission not found");
        }

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
        submissionMapper.update(submission);
        
        // Set other submissions for this registration as invalid
        List<Submission> otherSubmissions = submissionMapper.findByRegistrationId(submission.getRegistrationId());
        for (Submission other : otherSubmissions) {
            if (!other.getSubmissionId().equals(id) && other.getFinalLockedAt() == null) {
                other.setSubmissionStatus("invalid");
                submissionMapper.update(other);
            }
        }

        return submission;
    }
    
    /**
     * Check if user can modify submission (create, edit, submit, lock)
     * For individual registration: user must be the registrant
     * For team registration: user must be the team leader
     */
    public boolean canUserModifySubmission(Integer registrationId, Integer userId) {
        Registration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new RuntimeException("Registration not found");
        }
        
        // Individual registration
        if (registration.getUserId() != null && registration.getTeamId() == null) {
            return registration.getUserId().equals(userId);
        }
        
        // Team registration - check if user is team leader
        if (registration.getTeamId() != null) {
            TeamMember member = teamMemberMapper.findById(registration.getTeamId(), userId);
            if (member != null) {
                return TeamService.ROLE_LEADER.equals(member.getRoleInTeam());
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
        Registration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new RuntimeException("Registration not found");
        }
        
        // Individual registration
        if (registration.getUserId() != null && registration.getTeamId() == null) {
            return registration.getUserId().equals(userId);
        }
        
        // Team registration - check if user is any team member
        if (registration.getTeamId() != null) {
            TeamMember member = teamMemberMapper.findById(registration.getTeamId(), userId);
            return member != null;
        }
        
        return false;
    }

    public List<Submission> getSubmissionsByRegistration(Integer registrationId) {
        return submissionMapper.findByRegistrationId(registrationId);
    }
}
