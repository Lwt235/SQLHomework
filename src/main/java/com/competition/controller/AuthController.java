package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.dto.JwtResponse;
import com.competition.dto.LoginRequest;
import com.competition.dto.RegisterRequest;
import com.competition.entity.User;
import com.competition.service.AuthService;
import com.competition.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("Registration successful", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid token"));
            }
            
            Integer userId = jwtTokenProvider.getUserIdFromToken(token);
            authService.deactivateAccount(userId);
            return ResponseEntity.ok(ApiResponse.success("账号已注销成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("注销失败: " + e.getMessage()));
        }
    }
}
