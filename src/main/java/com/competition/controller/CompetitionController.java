package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.dto.CompetitionWithAwardsRequest;
import com.competition.entity.Competition;
import com.competition.service.CompetitionService;
import com.competition.service.CompetitionStatusScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@CrossOrigin(origins = "*")
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;
    
    @Autowired
    private CompetitionStatusScheduler competitionStatusScheduler;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Competition>>> getAllCompetitions() {
        List<Competition> competitions = competitionService.getAllCompetitions();
        return ResponseEntity.ok(ApiResponse.success(competitions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Competition>> getCompetitionById(@PathVariable Integer id) {
        return competitionService.getCompetitionById(id)
                .map(competition -> ResponseEntity.ok(ApiResponse.success(competition)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Competition>> createCompetition(@RequestBody Competition competition) {
        try {
            Competition created = competitionService.createCompetition(competition);
            return ResponseEntity.ok(ApiResponse.success("Competition created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create competition: " + e.getMessage()));
        }
    }
    
    @PostMapping("/with-awards")
    public ResponseEntity<ApiResponse<Competition>> createCompetitionWithAwards(@RequestBody CompetitionWithAwardsRequest request) {
        try {
            Competition created = competitionService.createCompetitionWithAwards(request.getCompetition(), request.getAwards());
            return ResponseEntity.ok(ApiResponse.success("Competition and awards created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create competition with awards: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Competition>> updateCompetition(
            @PathVariable Integer id,
            @RequestBody Competition competition) {
        try {
            Competition updated = competitionService.updateCompetition(id, competition);
            return ResponseEntity.ok(ApiResponse.success("Competition updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update competition: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCompetition(@PathVariable Integer id) {
        try {
            competitionService.deleteCompetition(id);
            return ResponseEntity.ok(ApiResponse.success("Competition deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete competition: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Competition>>> getCompetitionsByStatus(@PathVariable String status) {
        List<Competition> competitions = competitionService.getCompetitionsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(competitions));
    }
    
    /**
     * Manually trigger competition status update for all competitions
     * This allows administrators to immediately refresh competition statuses
     */
    @PostMapping("/refresh-status")
    public ResponseEntity<ApiResponse<Void>> refreshCompetitionStatuses() {
        try {
            competitionStatusScheduler.manualUpdateCompetitionStatuses();
            return ResponseEntity.ok(ApiResponse.success("Competition statuses refreshed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to refresh competition statuses: " + e.getMessage()));
        }
    }
}
