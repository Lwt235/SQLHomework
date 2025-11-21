package com.competition.service;

import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.repository.UserRepository;
import com.competition.repository.UserRoleRepository;
import com.competition.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    // User status constants
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String STATUS_SUSPENDED = "suspended";
    private static final List<String> ALLOWED_STATUSES = Arrays.asList(STATUS_ACTIVE, STATUS_INACTIVE, STATUS_SUSPENDED);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getDeleted())
                .collect(Collectors.toList());
    }

    public List<User> getInactiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getDeleted() && STATUS_INACTIVE.equals(user.getUserStatus()))
                .collect(Collectors.toList());
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserStatus(Integer userId, String status) {
        User user = getUserById(userId);
        
        // Validate status value
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("用户状态不能为空");
        }
        
        // Validate status is one of the allowed values
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new RuntimeException("无效的用户状态: " + status);
        }
        
        user.setUserStatus(status);
        return userRepository.save(user);
    }

    public User activateUser(Integer userId) {
        return updateUserStatus(userId, STATUS_ACTIVE);
    }

    public User suspendUser(Integer userId) {
        return updateUserStatus(userId, STATUS_SUSPENDED);
    }

    public void deleteUser(Integer userId) {
        User user = getUserById(userId);
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Transactional
    public void assignRoles(Integer userId, List<Integer> roleIds) {
        User user = getUserById(userId);
        
        // Remove existing roles
        List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
        if (!existingRoles.isEmpty()) {
            userRoleRepository.deleteAll(existingRoles);
        }
        
        // Add new roles
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                throw new RuntimeException("Some role IDs are invalid");
            }
            
            List<UserRole> newUserRoles = new java.util.ArrayList<>();
            for (Integer roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                newUserRoles.add(userRole);
            }
            userRoleRepository.saveAll(newUserRoles);
        }
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll().stream()
                .filter(role -> !role.getDeleted())
                .collect(Collectors.toList());
    }

    public List<Role> getUserRoles(Integer userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        if (roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        return roleRepository.findAllById(roleIds).stream()
                .filter(role -> !role.getDeleted())
                .collect(Collectors.toList());
    }
}
