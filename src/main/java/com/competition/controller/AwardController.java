package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.entity.Award;
import com.competition.entity.AwardResult;
import com.competition.service.AwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/awards")
@CrossOrigin(origins = "*")
public class AwardController {

    @Autowired
    private AwardService awardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Award>>> getAllAwards() {
        List<Award> awards = awardService.getAllAwards();
        return ResponseEntity.ok(ApiResponse.success(awards));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Award>> getAwardById(@PathVariable Integer id) {
        return awardService.getAwardById(id)
                .map(award -> ResponseEntity.ok(ApiResponse.success(award)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Award>> createAward(@RequestBody Award award) {
        try {
            Award created = awardService.createAward(award);
            return ResponseEntity.ok(ApiResponse.success("Award created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create award: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Award>> updateAward(
            @PathVariable Integer id,
            @RequestBody Award award) {
        try {
            Award updated = awardService.updateAward(id, award);
            return ResponseEntity.ok(ApiResponse.success("Award updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update award: " + e.getMessage()));
        }
    }

    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<ApiResponse<List<Award>>> getAwardsByCompetition(@PathVariable Integer competitionId) {
        List<Award> awards = awardService.getAwardsByCompetition(competitionId);
        return ResponseEntity.ok(ApiResponse.success(awards));
    }

    @PostMapping("/grant")
    public ResponseEntity<ApiResponse<AwardResult>> grantAward(
            @RequestParam Integer registrationId,
            @RequestParam Integer awardId,
            @RequestParam String certificateNo) {
        try {
            AwardResult result = awardService.grantAward(registrationId, awardId, certificateNo);
            return ResponseEntity.ok(ApiResponse.success("Award granted successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to grant award: " + e.getMessage()));
        }
    }

    @GetMapping("/results/registration/{registrationId}")
    public ResponseEntity<ApiResponse<List<AwardResult>>> getAwardResultsByRegistration(
            @PathVariable Integer registrationId) {
        List<AwardResult> results = awardService.getAwardResultsByRegistration(registrationId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/results/award/{awardId}")
    public ResponseEntity<ApiResponse<List<AwardResult>>> getAwardResultsByAward(@PathVariable Integer awardId) {
        List<AwardResult> results = awardService.getAwardResultsByAward(awardId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
