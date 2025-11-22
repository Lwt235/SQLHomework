package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.entity.User;
import com.competition.entity.Role;
import com.competition.service.UserService;
import com.competition.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * User management controller for admin operations
 * All operations require ADMIN role for security
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Get all users (Admin only) with optional filtering and sorting
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        try {
            List<User> users = userService.getAllUsersFiltered(username, school, roleCode, sortBy, sortOrder);
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getInactiveUsers() {
        try {
            List<User> users = userService.getInactiveUsers();
            return ResponseEntity.ok(ApiResponse.success("Inactive users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve inactive users: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<User>> updateUserProfile(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates,
            HttpServletRequest request) {
        try {
            // Get current user ID from JWT token
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
            }
            
            Integer currentUserId = jwtTokenProvider.getUserIdFromToken(token);
            
            // Users can only update their own profile, unless they're admin
            if (!currentUserId.equals(id)) {
                return ResponseEntity.status(403).body(ApiResponse.error("只能修改自己的个人信息"));
            }
            
            User user = userService.updateUserProfile(id, updates);
            return ResponseEntity.ok(ApiResponse.success("个人信息更新成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> activateUser(@PathVariable Integer id) {
        try {
            User user = userService.activateUser(id);
            return ResponseEntity.ok(ApiResponse.success("User activated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to activate user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> suspendUser(@PathVariable Integer id) {
        try {
            User user = userService.suspendUser(id);
            return ResponseEntity.ok(ApiResponse.success("User suspended successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to suspend user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        try {
            User user = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update user status: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id, HttpServletRequest request) {
        try {
            // Get current user ID from JWT token
            String token = jwtTokenProvider.resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Integer currentUserId = jwtTokenProvider.getUserIdFromToken(token);
                
                // Prevent self-deletion
                if (currentUserId.equals(id)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("不能删除自己的账号"));
                }
            }
            
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        try {
            List<Role> roles = userService.getAllRoles();
            return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve roles: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getUserRoles(@PathVariable Integer id) {
        try {
            List<Role> roles = userService.getUserRoles(id);
            return ResponseEntity.ok(ApiResponse.success("User roles retrieved successfully", roles));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve user roles: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRoles(
            @PathVariable Integer id, 
            @RequestBody Map<String, List<Integer>> payload) {
        try {
            List<Integer> roleIds = payload.get("roleIds");
            userService.assignRoles(id, roleIds);
            return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to assign roles: " + e.getMessage()));
        }
    }
}
