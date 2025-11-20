package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.entity.Submission;
import com.competition.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Submission>>> getAllSubmissions() {
        List<Submission> submissions = submissionService.getAllSubmissions();
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Submission>> getSubmissionById(@PathVariable Integer id) {
        return submissionService.getSubmissionById(id)
                .map(submission -> ResponseEntity.ok(ApiResponse.success(submission)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Submission>> createSubmission(@RequestBody Submission submission) {
        try {
            Submission created = submissionService.createSubmission(submission);
            return ResponseEntity.ok(ApiResponse.success("Submission created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create submission: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Submission>> updateSubmission(
            @PathVariable Integer id,
            @RequestBody Submission submission) {
        try {
            Submission updated = submissionService.updateSubmission(id, submission);
            return ResponseEntity.ok(ApiResponse.success("Submission updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update submission: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Submission>> submitWork(@PathVariable Integer id) {
        try {
            Submission submitted = submissionService.submitWork(id);
            return ResponseEntity.ok(ApiResponse.success("Work submitted successfully", submitted));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to submit work: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Submission>> lockSubmission(@PathVariable Integer id) {
        try {
            Submission locked = submissionService.lockSubmission(id);
            return ResponseEntity.ok(ApiResponse.success("Submission locked successfully", locked));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to lock submission: " + e.getMessage()));
        }
    }

    @GetMapping("/registration/{registrationId}")
    public ResponseEntity<ApiResponse<List<Submission>>> getSubmissionsByRegistration(
            @PathVariable Integer registrationId) {
        List<Submission> submissions = submissionService.getSubmissionsByRegistration(registrationId);
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }
}
