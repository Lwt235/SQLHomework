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
import java.util.Map;
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
    
    public List<User> getAllUsersFiltered(String username, String school, String roleCode, String sortBy, String sortOrder) {
        List<User> users = getAllUsers();
        
        // Apply filters
        if (username != null && !username.trim().isEmpty()) {
            String lowerUsername = username.toLowerCase();
            users = users.stream()
                    .filter(u -> u.getUsername() != null && u.getUsername().toLowerCase().contains(lowerUsername))
                    .collect(Collectors.toList());
        }
        
        if (school != null && !school.trim().isEmpty()) {
            String lowerSchool = school.toLowerCase();
            users = users.stream()
                    .filter(u -> u.getSchool() != null && u.getSchool().toLowerCase().contains(lowerSchool))
                    .collect(Collectors.toList());
        }
        
        if (roleCode != null && !roleCode.trim().isEmpty()) {
            // Filter by role
            Role role = roleRepository.findAll().stream()
                    .filter(r -> roleCode.equalsIgnoreCase(r.getRoleCode()))
                    .findFirst()
                    .orElse(null);
            
            if (role != null) {
                List<UserRole> userRolesForRole = userRoleRepository.findByRoleId(role.getRoleId());
                List<Integer> userIdsWithRole = userRolesForRole.stream()
                        .map(UserRole::getUserId)
                        .collect(Collectors.toList());
                
                users = users.stream()
                        .filter(u -> userIdsWithRole.contains(u.getUserId()))
                        .collect(Collectors.toList());
            }
        }
        
        // Apply sorting
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            boolean ascending = !"desc".equalsIgnoreCase(sortOrder);
            
            switch (sortBy.toLowerCase()) {
                case "username":
                    users.sort((u1, u2) -> {
                        String name1 = u1.getUsername() != null ? u1.getUsername() : "";
                        String name2 = u2.getUsername() != null ? u2.getUsername() : "";
                        return ascending ? name1.compareTo(name2) : name2.compareTo(name1);
                    });
                    break;
                case "school":
                    users.sort((u1, u2) -> {
                        String school1 = u1.getSchool() != null ? u1.getSchool() : "";
                        String school2 = u2.getSchool() != null ? u2.getSchool() : "";
                        return ascending ? school1.compareTo(school2) : school2.compareTo(school1);
                    });
                    break;
                case "createdat":
                    users.sort((u1, u2) -> {
                        int result = u1.getCreatedAt().compareTo(u2.getCreatedAt());
                        return ascending ? result : -result;
                    });
                    break;
                case "userstatus":
                    users.sort((u1, u2) -> {
                        String status1 = u1.getUserStatus() != null ? u1.getUserStatus() : "";
                        String status2 = u2.getUserStatus() != null ? u2.getUserStatus() : "";
                        return ascending ? status1.compareTo(status2) : status2.compareTo(status1);
                    });
                    break;
            }
        }
        
        return users;
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

    public User updateUserProfile(Integer userId, Map<String, Object> updates) {
        User user = getUserById(userId);
        
        // Check if username is being updated
        if (updates.containsKey("username")) {
            String newUsername = (String) updates.get("username");
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                // Check if username is already used by another user
                userRepository.findByUsername(newUsername).ifPresent(existingUser -> {
                    if (!existingUser.getUserId().equals(userId)) {
                        throw new RuntimeException("用户名已被其他用户使用");
                    }
                });
                user.setUsername(newUsername);
            }
        }
        
        // Update allowed fields
        if (updates.containsKey("realName")) {
            String realName = (String) updates.get("realName");
            user.setRealName(realName);
        }
        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            if (email != null && !email.isEmpty()) {
                // Check if email is already used by another user
                userRepository.findByEmail(email).ifPresent(existingUser -> {
                    if (!existingUser.getUserId().equals(userId)) {
                        throw new RuntimeException("邮箱已被其他用户使用");
                    }
                });
            }
            user.setEmail(email);
        }
        if (updates.containsKey("phone")) {
            String phone = (String) updates.get("phone");
            user.setPhone(phone);
        }
        if (updates.containsKey("school")) {
            String school = (String) updates.get("school");
            user.setSchool(school);
        }
        if (updates.containsKey("department")) {
            String department = (String) updates.get("department");
            user.setDepartment(department);
        }
        if (updates.containsKey("studentNo")) {
            String studentNo = (String) updates.get("studentNo");
            user.setStudentNo(studentNo);
        }
        
        return userRepository.save(user);
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
