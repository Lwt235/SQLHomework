package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.entity.Team;
import com.competition.entity.TeamMember;
import com.competition.service.TeamService;
import com.competition.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {
    
    @Autowired
    private TeamService teamService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Team>>> getAllTeams() {
        try {
            List<Team> teams = teamService.getAllTeams();
            return ResponseEntity.ok(ApiResponse.success("Teams retrieved successfully", teams));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve teams: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> getTeamById(@PathVariable Integer id) {
        try {
            Team team = teamService.getTeamById(id);
            return ResponseEntity.ok(ApiResponse.success("Team retrieved successfully", team));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve team: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<TeamMember>>> getTeamMembers(@PathVariable Integer id) {
        try {
            List<TeamMember> members = teamService.getTeamMembers(id);
            return ResponseEntity.ok(ApiResponse.success("Team members retrieved successfully", members));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve team members: " + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Team>>> getTeamsByUser(@PathVariable Integer userId) {
        try {
            List<Team> teams = teamService.getTeamsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("User teams retrieved successfully", teams));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve user teams: " + e.getMessage()));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Team>> createTeam(
            @RequestBody Team team, 
            HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
            }
            
            Integer userId = jwtTokenProvider.getUserIdFromToken(token);
            Team created = teamService.createTeam(team, userId);
            return ResponseEntity.ok(ApiResponse.success("Team created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create team: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Team>> updateTeam(
            @PathVariable Integer id,
            @RequestBody Team team) {
        try {
            Team updated = teamService.updateTeam(id, team);
            return ResponseEntity.ok(ApiResponse.success("Team updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update team: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Integer id) {
        try {
            teamService.deleteTeam(id);
            return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete team: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> addTeamMember(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        try {
            Integer userId = (Integer) payload.get("userId");
            String roleInTeam = (String) payload.getOrDefault("roleInTeam", "member");
            teamService.addTeamMember(id, userId, roleInTeam);
            return ResponseEntity.ok(ApiResponse.success("Member added successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add member: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> removeTeamMember(
            @PathVariable Integer teamId,
            @PathVariable Integer userId) {
        try {
            teamService.removeTeamMember(teamId, userId);
            return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to remove member: " + e.getMessage()));
        }
    }
}
