package com.competition.service;

import com.competition.dto.JwtResponse;
import com.competition.dto.LoginRequest;
import com.competition.dto.RegisterRequest;
import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.repository.UserRepository;
import com.competition.repository.UserRoleRepository;
import com.competition.repository.RoleRepository;
import com.competition.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = tokenProvider.generateToken(user.getUsername(), user.getUserId());

        // Get user roles - optimized to avoid N+1 queries
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getUserId());
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        List<String> roleCodes = roleIds.isEmpty() ? 
                java.util.Collections.emptyList() :
                roleRepository.findAllById(roleIds).stream()
                        .map(Role::getRoleCode)
                        .collect(Collectors.toList());

        return new JwtResponse(token, user.getUserId(), user.getUsername(), roleCodes);
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSchool(request.getSchool());
        user.setDepartment(request.getDepartment());
        user.setStudentNo(request.getStudentNo());
        user.setUserStatus("inactive");  // Changed from "active" to "inactive" - requires admin approval
        user.setAuthType("local");

        return userRepository.save(user);
    }

    @Transactional
    public void deactivateAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has admin role
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(roleIds);
            boolean isAdmin = roles.stream()
                    .anyMatch(role -> "admin".equalsIgnoreCase(role.getRoleCode()));
            
            if (isAdmin) {
                throw new RuntimeException("管理员账号不允许注销");
            }
        }
        
        // Soft delete the account
        user.setDeleted(true);
        userRepository.save(user);
    }
}
