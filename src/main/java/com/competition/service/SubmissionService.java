package com.competition.service;

import com.competition.entity.Submission;
import com.competition.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

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

        submission.setSubmissionTitle(submissionDetails.getSubmissionTitle());
        submission.setAbstractText(submissionDetails.getAbstractText());
        submission.setSubmissionStatus(submissionDetails.getSubmissionStatus());

        return submissionRepository.save(submission);
    }

    public Submission submitWork(Integer id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setSubmissionStatus("submitted");
        submission.setSubmittedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    public Submission lockSubmission(Integer id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setFinalLockedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    public List<Submission> getSubmissionsByRegistration(Integer registrationId) {
        return submissionRepository.findByRegistrationId(registrationId);
    }
}
