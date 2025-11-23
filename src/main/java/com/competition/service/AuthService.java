package com.competition.service;

import com.competition.dto.JwtResponse;
import com.competition.dto.LoginRequest;
import com.competition.dto.RegisterRequest;
import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.mapper.UserMapper;
import com.competition.mapper.UserRoleMapper;
import com.competition.mapper.RoleMapper;
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
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    public JwtResponse login(LoginRequest request) {
        // First check if user exists and get their status
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // Check if user is deleted
        if (user.getDeleted()) {
            throw new RuntimeException("该账号已被删除");
        }
        
        // Check if user is inactive (awaiting admin approval)
        if (UserService.STATUS_INACTIVE.equals(user.getUserStatus())) {
            throw new RuntimeException("您的账号正在等待管理员审核，请稍后再试");
        }
        
        // Check if user is suspended
        if (UserService.STATUS_SUSPENDED.equals(user.getUserStatus())) {
            throw new RuntimeException("您的账号已被暂停，请联系管理员");
        }
        
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = tokenProvider.generateToken(user.getUsername(), user.getUserId());

        // Get user roles - optimized to avoid N+1 queries
        List<UserRole> userRoles = userRoleMapper.findByUserId(user.getUserId());
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        List<String> roleCodes = roleIds.isEmpty() ? 
                java.util.Collections.emptyList() :
                roleIds.stream()
                        .map(roleId -> roleMapper.findById(roleId))
                        .filter(role -> role != null)
                        .map(Role::getRoleCode)
                        .collect(Collectors.toList());

        return new JwtResponse(token, user.getUserId(), user.getUsername(), roleCodes);
    }

    public User register(RegisterRequest request) {
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userMapper.existsByEmail(request.getEmail())) {
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
        user.setUserStatus(UserService.STATUS_INACTIVE);  // Changed from "active" to "inactive" - requires admin approval
        user.setAuthType("local");

        userMapper.insert(user);
        return user;
    }

    @Transactional
    public void deactivateAccount(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user has admin role
        List<UserRole> userRoles = userRoleMapper.findByUserId(userId);
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleIds.stream()
                    .map(roleId -> roleMapper.findById(roleId))
                    .filter(role -> role != null)
                    .collect(Collectors.toList());
            
            boolean isAdmin = roles.stream()
                    .anyMatch(role -> "admin".equalsIgnoreCase(role.getRoleCode()));
            
            if (isAdmin) {
                throw new RuntimeException("管理员账号不允许注销");
            }
        }
        
        // Soft delete the account
        user.setDeleted(true);
        userMapper.update(user);
    }
}
