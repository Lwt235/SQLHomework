package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.entity.JudgeAssignment;
import com.competition.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/judge")
@CrossOrigin(origins = "*")
public class JudgeController {

    @Autowired
    private JudgeService judgeService;

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<JudgeAssignment>>> getAllAssignments() {
        List<JudgeAssignment> assignments = judgeService.getAllAssignments();
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<JudgeAssignment>> createAssignment(@RequestBody JudgeAssignment assignment) {
        try {
            JudgeAssignment created = judgeService.createAssignment(assignment);
            return ResponseEntity.ok(ApiResponse.success("Assignment created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create assignment: " + e.getMessage()));
        }
    }

    @PutMapping("/assignments")
    public ResponseEntity<ApiResponse<JudgeAssignment>> updateAssignment(@RequestBody JudgeAssignment assignment) {
        try {
            JudgeAssignment updated = judgeService.updateAssignment(assignment);
            return ResponseEntity.ok(ApiResponse.success("Assignment updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update assignment: " + e.getMessage()));
        }
    }

    @GetMapping("/assignments/judge/{userId}")
    public ResponseEntity<ApiResponse<List<JudgeAssignment>>> getAssignmentsByJudge(@PathVariable Integer userId) {
        List<JudgeAssignment> assignments = judgeService.getAssignmentsByJudge(userId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @GetMapping("/assignments/submission/{submissionId}")
    public ResponseEntity<ApiResponse<List<JudgeAssignment>>> getAssignmentsBySubmission(
            @PathVariable Integer submissionId) {
        List<JudgeAssignment> assignments = judgeService.getAssignmentsBySubmission(submissionId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }
}
