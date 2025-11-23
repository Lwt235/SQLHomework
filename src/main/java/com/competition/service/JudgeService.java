package com.competition.service;

import com.competition.entity.JudgeAssignment;
import com.competition.entity.Submission;
import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.mapper.JudgeAssignmentMapper;
import com.competition.mapper.SubmissionMapper;
import com.competition.mapper.UserMapper;
import com.competition.mapper.UserRoleMapper;
import com.competition.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class JudgeService {

    @Autowired
    private JudgeAssignmentMapper judgeAssignmentMapper;
    
    @Autowired
    private SubmissionMapper submissionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    public List<JudgeAssignment> getAllAssignments() {
        return judgeAssignmentMapper.findAll();
    }

    public JudgeAssignment createAssignment(JudgeAssignment assignment) {
        judgeAssignmentMapper.insert(assignment);
        return assignment;
    }

    public JudgeAssignment updateAssignment(JudgeAssignment assignment) {
        // Get existing assignment to preserve created_at and update judgeStatus
        JudgeAssignment existing = judgeAssignmentMapper.findById(assignment.getUserId(), assignment.getSubmissionId());
        if (existing == null) {
            throw new RuntimeException("评审任务不存在");
        }
        
        // Update fields
        existing.setScore(assignment.getScore());
        existing.setComment(assignment.getComment());
        existing.setWeight(assignment.getWeight());
        
        // Set status to "reviewed" if score is provided and not yet completed
        if (assignment.getScore() != null && !"completed".equals(existing.getJudgeStatus())) {
            existing.setJudgeStatus("reviewed");
        }
        
        judgeAssignmentMapper.update(existing);
        return existing;
    }

    public List<JudgeAssignment> getAssignmentsByJudge(Integer userId) {
        return judgeAssignmentMapper.findByUserId(userId);
    }

    public List<JudgeAssignment> getAssignmentsBySubmission(Integer submissionId) {
        return judgeAssignmentMapper.findBySubmissionId(submissionId);
    }
    
    /**
     * Manually assign a judge to a submission
     */
    @Transactional
    public JudgeAssignment assignJudgeToSubmission(Integer judgeUserId, Integer submissionId, BigDecimal weight) {
        // Validate judge user exists and has TEACHER role
        User judge = userMapper.findById(judgeUserId);
        if (judge == null) {
            throw new RuntimeException("评审教师不存在");
        }
        
        if (!hasTeacherRole(judgeUserId)) {
            throw new RuntimeException("只有教师用户才能被分配评审任务");
        }
        
        // Validate submission exists
        Submission submission = submissionMapper.findById(submissionId);
        if (submission == null) {
            throw new RuntimeException("作品不存在");
        }
        
        if (!"locked".equals(submission.getSubmissionStatus())) {
            throw new RuntimeException("只有已锁定的作品才能分配评审");
        }
        
        // Check if assignment already exists
        JudgeAssignment existing = judgeAssignmentMapper.findById(judgeUserId, submissionId);
        if (existing != null) {
            throw new RuntimeException("该评审已被分配给此作品");
        }
        
        // Create new assignment
        JudgeAssignment assignment = new JudgeAssignment();
        assignment.setUserId(judgeUserId);
        assignment.setSubmissionId(submissionId);
        assignment.setWeight(weight != null ? weight : BigDecimal.ONE);
        assignment.setScore(BigDecimal.ZERO);
        
        judgeAssignmentMapper.insert(assignment);
        return assignment;
    }
    
    /**
     * Randomly assign judges to locked submissions
     * @param judgesPerSubmission Number of judges to assign per submission
     * @return Number of assignments created
     */
    @Transactional
    public int randomAssignJudges(int judgesPerSubmission) {
        return randomAssignJudgesFiltered(judgesPerSubmission, null, null, null);
    }
    
    /**
     * Randomly assign judges to specific submission(s) with teacher filtering
     * @param judgesPerSubmission Number of judges to assign per submission
     * @param submissionId Specific submission ID (null for all locked submissions)
     * @param includeTeacherIds List of teacher IDs to include (null for all)
     * @param excludeTeacherIds List of teacher IDs to exclude (null for none)
     * @return Number of assignments created
     */
    @Transactional
    public int randomAssignJudgesFiltered(int judgesPerSubmission, Integer submissionId, 
                                          List<Integer> includeTeacherIds, List<Integer> excludeTeacherIds) {
        if (judgesPerSubmission <= 0) {
            throw new RuntimeException("每个作品至少需要分配1个评审");
        }
        
        // Get all teachers (judges)
        List<User> teachers = getAllTeachers();
        if (teachers.isEmpty()) {
            throw new RuntimeException("系统中没有教师用户可以分配评审任务");
        }
        
        // Apply teacher filters
        if (includeTeacherIds != null && !includeTeacherIds.isEmpty()) {
            teachers = teachers.stream()
                    .filter(t -> includeTeacherIds.contains(t.getUserId()))
                    .collect(Collectors.toList());
        }
        
        if (excludeTeacherIds != null && !excludeTeacherIds.isEmpty()) {
            teachers = teachers.stream()
                    .filter(t -> !excludeTeacherIds.contains(t.getUserId()))
                    .collect(Collectors.toList());
        }
        
        if (teachers.isEmpty()) {
            throw new RuntimeException("根据筛选条件没有可用的教师");
        }
        
        if (teachers.size() < judgesPerSubmission) {
            throw new RuntimeException("可用教师用户数量不足，无法完成分配");
        }
        
        // Get submissions to assign
        List<Submission> lockedSubmissions;
        if (submissionId != null) {
            // Assign to specific submission
            Submission submission = submissionMapper.findById(submissionId);
            if (submission == null) {
                throw new RuntimeException("作品不存在");
            }
            
            if (!"locked".equals(submission.getSubmissionStatus())) {
                throw new RuntimeException("只有已锁定的作品才能分配评审");
            }
            
            lockedSubmissions = java.util.Collections.singletonList(submission);
        } else {
            // Assign to all locked submissions
            lockedSubmissions = submissionMapper.findBySubmissionStatus("locked");
            if (lockedSubmissions.isEmpty()) {
                throw new RuntimeException("没有需要分配评审的锁定作品");
            }
        }
        
        int assignmentCount = 0;
        Random random = new Random();
        
        for (Submission submission : lockedSubmissions) {
            // Get existing assignments for this submission
            List<JudgeAssignment> existingAssignments = judgeAssignmentMapper.findBySubmissionId(submission.getSubmissionId());
            
            // If already has enough judges, skip
            if (existingAssignments.size() >= judgesPerSubmission) {
                continue;
            }
            
            // Get teachers who are not already assigned to this submission
            List<Integer> assignedTeacherIds = existingAssignments.stream()
                    .map(JudgeAssignment::getUserId)
                    .collect(Collectors.toList());
            
            List<User> availableTeachers = teachers.stream()
                    .filter(t -> !assignedTeacherIds.contains(t.getUserId()))
                    .collect(Collectors.toList());
            
            // Calculate how many more judges needed
            int needCount = judgesPerSubmission - existingAssignments.size();
            needCount = Math.min(needCount, availableTeachers.size());
            
            // Randomly select teachers
            List<User> selectedTeachers = new ArrayList<>(availableTeachers);
            while (selectedTeachers.size() > needCount) {
                selectedTeachers.remove(random.nextInt(selectedTeachers.size()));
            }
            
            // Create assignments
            BigDecimal weight = BigDecimal.ONE.divide(BigDecimal.valueOf(judgesPerSubmission), 2, RoundingMode.HALF_UP);
            for (User teacher : selectedTeachers) {
                JudgeAssignment assignment = new JudgeAssignment();
                assignment.setUserId(teacher.getUserId());
                assignment.setSubmissionId(submission.getSubmissionId());
                assignment.setWeight(weight);
                assignment.setScore(BigDecimal.ZERO);
                judgeAssignmentMapper.insert(assignment);
                assignmentCount++;
            }
        }
        
        return assignmentCount;
    }
    
    /**
     * Get all users with TEACHER role
     */
    private List<User> getAllTeachers() {
        // Find TEACHER role
        List<Role> roles = roleMapper.findAll();
        Role teacherRole = roles.stream()
                .filter(r -> "TEACHER".equalsIgnoreCase(r.getRoleCode()))
                .findFirst()
                .orElse(null);
        
        if (teacherRole == null) {
            return new ArrayList<>();
        }
        
        // Find all users with TEACHER role
        List<UserRole> teacherUserRoles = userRoleMapper.findByRoleId(teacherRole.getRoleId());
        List<Integer> teacherUserIds = teacherUserRoles.stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
        
        if (teacherUserIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Return active, non-deleted teachers
        return teacherUserIds.stream()
                .map(id -> userMapper.findById(id))
                .filter(u -> u != null && !u.getDeleted() && UserService.STATUS_ACTIVE.equals(u.getUserStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if user has TEACHER role
     */
    private boolean hasTeacherRole(Integer userId) {
        List<UserRole> userRoles = userRoleMapper.findByUserId(userId);
        
        for (UserRole userRole : userRoles) {
            Role role = roleMapper.findById(userRole.getRoleId());
            if (role != null && "TEACHER".equalsIgnoreCase(role.getRoleCode())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Confirm review completion - locks the score and comment
     */
    @Transactional
    public JudgeAssignment confirmReview(Integer userId, Integer submissionId) {
        JudgeAssignment assignment = judgeAssignmentMapper.findById(userId, submissionId);
        if (assignment == null) {
            throw new RuntimeException("评审任务不存在");
        }
        
        if ("completed".equals(assignment.getJudgeStatus())) {
            throw new RuntimeException("该评审已经确认完成，无法重复确认");
        }
        
        if (assignment.getScore() == null) {
            throw new RuntimeException("请先完成评分后再确认");
        }
        
        assignment.setJudgeStatus("completed");
        judgeAssignmentMapper.update(assignment);
        return assignment;
    }
}
