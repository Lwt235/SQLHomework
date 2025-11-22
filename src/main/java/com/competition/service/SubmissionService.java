package com.competition.service;

import com.competition.entity.Submission;
import com.competition.entity.JudgeAssignment;
import com.competition.repository.SubmissionRepository;
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
    private JudgeAssignmentRepository judgeAssignmentRepository;

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public Optional<Submission> getSubmissionById(Integer id) {
        return submissionRepository.findById(id);
    }

    public Submission createSubmission(Submission submission) {
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

    public Submission submitWork(Integer id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Check if submission is locked
        if (submission.getFinalLockedAt() != null) {
            throw new RuntimeException("作品已最终锁定，无法再修改");
        }

        submission.setSubmissionStatus("submitted");
        submission.setSubmittedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission lockSubmission(Integer id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Check if already locked
        if (submission.getFinalLockedAt() != null) {
            throw new RuntimeException("作品已经最终锁定");
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

    public List<Submission> getSubmissionsByRegistration(Integer registrationId) {
        return submissionRepository.findByRegistrationId(registrationId);
    }
}
