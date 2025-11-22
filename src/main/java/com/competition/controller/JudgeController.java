package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.entity.JudgeAssignment;
import com.competition.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    
    /**
     * Manually assign a judge to a submission (Admin only)
     */
    @PostMapping("/assignments/manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JudgeAssignment>> manualAssignJudge(@RequestBody Map<String, Object> payload) {
        try {
            Integer judgeUserId = (Integer) payload.get("judgeUserId");
            Integer submissionId = (Integer) payload.get("submissionId");
            BigDecimal weight = payload.get("weight") != null 
                ? new BigDecimal(payload.get("weight").toString()) 
                : BigDecimal.ONE;
            
            if (judgeUserId == null || submissionId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("评审ID和作品ID不能为空"));
            }
            
            JudgeAssignment assignment = judgeService.assignJudgeToSubmission(judgeUserId, submissionId, weight);
            return ResponseEntity.ok(ApiResponse.success("评审分配成功", assignment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("评审分配失败: " + e.getMessage()));
        }
    }
    
    /**
     * Randomly assign judges to all locked submissions (Admin only)
     */
    @PostMapping("/assignments/random")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> randomAssignJudges(@RequestBody Map<String, Object> payload) {
        try {
            Integer judgesPerSubmission = (Integer) payload.getOrDefault("judgesPerSubmission", 3);
            
            if (judgesPerSubmission == null || judgesPerSubmission <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("每个作品至少需要分配1个评审"));
            }
            
            int assignmentCount = judgeService.randomAssignJudges(judgesPerSubmission);
            return ResponseEntity.ok(ApiResponse.success("成功随机分配 " + assignmentCount + " 个评审任务", assignmentCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("随机分配失败: " + e.getMessage()));
        }
    }
}
