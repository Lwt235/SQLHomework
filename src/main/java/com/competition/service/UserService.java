package com.competition.service;

import com.competition.entity.User;
import com.competition.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getIsDeleted())
                .collect(Collectors.toList());
    }

    public List<User> getInactiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getIsDeleted() && "inactive".equals(user.getUserStatus()))
                .collect(Collectors.toList());
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserStatus(Integer userId, String status) {
        User user = getUserById(userId);
        user.setUserStatus(status);
        return userRepository.save(user);
    }

    public User activateUser(Integer userId) {
        return updateUserStatus(userId, "active");
    }

    public User suspendUser(Integer userId) {
        return updateUserStatus(userId, "suspended");
    }

    public void deleteUser(Integer userId) {
        User user = getUserById(userId);
        user.setIsDeleted(true);
        userRepository.save(user);
    }
}
