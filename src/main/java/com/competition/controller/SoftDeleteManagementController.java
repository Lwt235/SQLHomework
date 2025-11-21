package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.entity.*;
import com.competition.service.SoftDeleteManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Soft-delete management controller for admin operations
 * Allows viewing and permanently deleting soft-deleted entities
 */
@RestController
@RequestMapping("/api/admin/soft-deleted")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class SoftDeleteManagementController {

    @Autowired
    private SoftDeleteManagementService softDeleteManagementService;

    /**
     * Get summary of all soft-deleted entities
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getSummary() {
        try {
            Map<String, Integer> summary = softDeleteManagementService.getSoftDeletedSummary();
            return ResponseEntity.ok(ApiResponse.success("Soft-deleted summary retrieved successfully", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve summary: " + e.getMessage()));
        }
    }

    /**
     * Get all soft-deleted users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getSoftDeletedUsers() {
        try {
            List<User> users = softDeleteManagementService.getSoftDeletedUsers();
            return ResponseEntity.ok(ApiResponse.success("Soft-deleted users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Get all soft-deleted competitions
     */
    @GetMapping("/competitions")
    public ResponseEntity<ApiResponse<List<Competition>>> getSoftDeletedCompetitions() {
        try {
            List<Competition> competitions = softDeleteManagementService.getSoftDeletedCompetitions();
            return ResponseEntity.ok(ApiResponse.success("Soft-deleted competitions retrieved successfully", competitions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve competitions: " + e.getMessage()));
        }
    }

    /**
     * Get all soft-deleted registrations
     */
    @GetMapping("/registrations")
    public ResponseEntity<ApiResponse<List<Registration>>> getSoftDeletedRegistrations() {
        try {
            List<Registration> registrations = softDeleteManagementService.getSoftDeletedRegistrations();
            return ResponseEntity.ok(ApiResponse.success("Soft-deleted registrations retrieved successfully", registrations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve registrations: " + e.getMessage()));
        }
    }

    /**
     * Get all soft-deleted submissions
     */
    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<List<Submission>>> getSoftDeletedSubmissions() {
        try {
            List<Submission> submissions = softDeleteManagementService.getSoftDeletedSubmissions();
            return ResponseEntity.ok(ApiResponse.success("Soft-deleted submissions retrieved successfully", submissions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve submissions: " + e.getMessage()));
        }
    }

    /**
     * Get all soft-deleted awards
     */
    @GetMapping("/awards")
    public ResponseEntity<ApiResponse<List<Award>>> getSoftDeletedAwards() {
        try {
            List<Award> awards = softDeleteManagementService.getSoftDeletedAwards();
            return ResponseEntity.ok(ApiResponse.success("Soft-deleted awards retrieved successfully", awards));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve awards: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete a single user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteUser(@PathVariable Integer id) {
        try {
            softDeleteManagementService.permanentlyDeleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete multiple users
     */
    @DeleteMapping("/users/batch")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteUsers(@RequestBody Map<String, List<Integer>> payload) {
        try {
            List<Integer> ids = payload.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No user IDs provided"));
            }
            softDeleteManagementService.permanentlyDeleteUsers(ids);
            return ResponseEntity.ok(ApiResponse.success("Users permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete users: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete a single competition
     */
    @DeleteMapping("/competitions/{id}")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteCompetition(@PathVariable Integer id) {
        try {
            softDeleteManagementService.permanentlyDeleteCompetition(id);
            return ResponseEntity.ok(ApiResponse.success("Competition permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete competition: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete multiple competitions
     */
    @DeleteMapping("/competitions/batch")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteCompetitions(@RequestBody Map<String, List<Integer>> payload) {
        try {
            List<Integer> ids = payload.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No competition IDs provided"));
            }
            softDeleteManagementService.permanentlyDeleteCompetitions(ids);
            return ResponseEntity.ok(ApiResponse.success("Competitions permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete competitions: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete a single registration
     */
    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteRegistration(@PathVariable Integer id) {
        try {
            softDeleteManagementService.permanentlyDeleteRegistration(id);
            return ResponseEntity.ok(ApiResponse.success("Registration permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete registration: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete multiple registrations
     */
    @DeleteMapping("/registrations/batch")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteRegistrations(@RequestBody Map<String, List<Integer>> payload) {
        try {
            List<Integer> ids = payload.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No registration IDs provided"));
            }
            softDeleteManagementService.permanentlyDeleteRegistrations(ids);
            return ResponseEntity.ok(ApiResponse.success("Registrations permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete registrations: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete a single submission
     */
    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteSubmission(@PathVariable Integer id) {
        try {
            softDeleteManagementService.permanentlyDeleteSubmission(id);
            return ResponseEntity.ok(ApiResponse.success("Submission permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete submission: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete multiple submissions
     */
    @DeleteMapping("/submissions/batch")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteSubmissions(@RequestBody Map<String, List<Integer>> payload) {
        try {
            List<Integer> ids = payload.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No submission IDs provided"));
            }
            softDeleteManagementService.permanentlyDeleteSubmissions(ids);
            return ResponseEntity.ok(ApiResponse.success("Submissions permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete submissions: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete a single award
     */
    @DeleteMapping("/awards/{id}")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteAward(@PathVariable Integer id) {
        try {
            softDeleteManagementService.permanentlyDeleteAward(id);
            return ResponseEntity.ok(ApiResponse.success("Award permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete award: " + e.getMessage()));
        }
    }

    /**
     * Permanently delete multiple awards
     */
    @DeleteMapping("/awards/batch")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteAwards(@RequestBody Map<String, List<Integer>> payload) {
        try {
            List<Integer> ids = payload.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No award IDs provided"));
            }
            softDeleteManagementService.permanentlyDeleteAwards(ids);
            return ResponseEntity.ok(ApiResponse.success("Awards permanently deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete awards: " + e.getMessage()));
        }
    }
}
