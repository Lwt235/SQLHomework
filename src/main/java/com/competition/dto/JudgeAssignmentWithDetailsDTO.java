package com.competition.dto;

import com.competition.entity.Competition;
import com.competition.entity.JudgeAssignment;
import com.competition.entity.Submission;
import com.competition.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for JudgeAssignment with enriched submission, competition, and user data
 */
@Data
public class JudgeAssignmentWithDetailsDTO {
    
    // From JudgeAssignment
    private Integer userId;
    private Integer submissionId;
    private BigDecimal weight;
    private BigDecimal score;
    private String comment;
    private String judgeStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enriched data
    private User user;
    private SubmissionWithDetailsDTO submission;
    
    public static JudgeAssignmentWithDetailsDTO from(JudgeAssignment assignment) {
        JudgeAssignmentWithDetailsDTO dto = new JudgeAssignmentWithDetailsDTO();
        dto.setUserId(assignment.getUserId());
        dto.setSubmissionId(assignment.getSubmissionId());
        dto.setWeight(assignment.getWeight());
        dto.setScore(assignment.getScore());
        dto.setComment(assignment.getComment());
        dto.setJudgeStatus(assignment.getJudgeStatus());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        return dto;
    }
    
    @Data
    public static class SubmissionWithDetailsDTO {
        private Integer submissionId;
        private Integer registrationId;
        private String submissionTitle;
        private String description; // This is the abstract field
        private String submissionStatus;
        private LocalDateTime submittedAt;
        private LocalDateTime finalLockedAt;
        private Competition competition;
        
        public static SubmissionWithDetailsDTO from(Submission submission) {
            SubmissionWithDetailsDTO dto = new SubmissionWithDetailsDTO();
            dto.setSubmissionId(submission.getSubmissionId());
            dto.setRegistrationId(submission.getRegistrationId());
            dto.setSubmissionTitle(submission.getSubmissionTitle());
            dto.setDescription(submission.getAbstractText());
            dto.setSubmissionStatus(submission.getSubmissionStatus());
            dto.setSubmittedAt(submission.getSubmittedAt());
            dto.setFinalLockedAt(submission.getFinalLockedAt());
            return dto;
        }
    }
}
