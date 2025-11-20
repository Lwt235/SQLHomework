package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.entity.Registration;
import com.competition.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Registration>>> getAllRegistrations() {
        List<Registration> registrations = registrationService.getAllRegistrations();
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Registration>> getRegistrationById(@PathVariable Integer id) {
        return registrationService.getRegistrationById(id)
                .map(registration -> ResponseEntity.ok(ApiResponse.success(registration)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Registration>> createRegistration(@RequestBody Registration registration) {
        try {
            Registration created = registrationService.createRegistration(registration);
            return ResponseEntity.ok(ApiResponse.success("Registration created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create registration: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Registration>> updateRegistrationStatus(
            @PathVariable Integer id,
            @RequestParam String status,
            @RequestParam Integer auditUserId) {
        try {
            Registration updated = registrationService.updateRegistrationStatus(id, status, auditUserId);
            return ResponseEntity.ok(ApiResponse.success("Registration status updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update status: " + e.getMessage()));
        }
    }

    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<ApiResponse<List<Registration>>> getRegistrationsByCompetition(
            @PathVariable Integer competitionId) {
        List<Registration> registrations = registrationService.getRegistrationsByCompetition(competitionId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Registration>>> getRegistrationsByUser(@PathVariable Integer userId) {
        List<Registration> registrations = registrationService.getRegistrationsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }
}
