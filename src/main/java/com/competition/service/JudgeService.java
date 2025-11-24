package com.competition.service;

import com.competition.dto.JudgeAssignmentWithDetailsDTO;
import com.competition.entity.Competition;
import com.competition.entity.JudgeAssignment;
import com.competition.entity.Registration;
import com.competition.entity.Submission;
import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.JudgeAssignmentMapper;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.SubmissionMapper;
import com.competition.mapper.UserMapper;
import com.competition.mapper.UserRoleMapper;
import com.competition.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
    
    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private TimeService timeService;

    public List<JudgeAssignment> getAllAssignments() {
        return judgeAssignmentMapper.findAll();
    }

    public JudgeAssignment createAssignment(JudgeAssignment assignment) {
        judgeAssignmentMapper.insert(assignment);
        return assignment;
    }
    
    /**
     * Check if the current time is in the review phase for the given competition.
     * Review phase is considered to be from submitStart to awardPublishStart.
     * 
     * Note: If review dates are not set, this method returns true to allow review at any time.
     * This is designed to be backward compatible with competitions that don't have review phase configured.
     * In production, you may want to enforce review dates as required fields.
     */
    private boolean isInReviewPhase(Competition competition) {
        if (competition == null || competition.getSubmitStart() == null || competition.getAwardPublishStart() == null) {
            // If review dates are not set, allow review at any time (backward compatibility)
            return true;
        }
        
        LocalDateTime currentTime = timeService.getCurrentTime();
        return !currentTime.isBefore(competition.getSubmitStart()) && !currentTime.isAfter(competition.getAwardPublishStart());
    }

    public JudgeAssignment updateAssignment(JudgeAssignment assignment) {
        // Get existing assignment to preserve created_at and weight
        JudgeAssignment existing = judgeAssignmentMapper.findById(assignment.getUserId(), assignment.getSubmissionId());
        if (existing == null) {
            throw new RuntimeException("评审任务不存在");
        }
        
        // Get submission and competition to check review phase
        Submission submission = submissionMapper.findById(assignment.getSubmissionId());
        if (submission == null) {
            throw new RuntimeException("作品不存在");
        }
        
        Registration registration = registrationMapper.findById(submission.getRegistrationId());
        if (registration == null) {
            throw new RuntimeException("报名信息不存在");
        }
        
        Competition competition = competitionMapper.findById(registration.getCompetitionId());
        if (!isInReviewPhase(competition)) {
            throw new RuntimeException("当前不在评审阶段，无法提交评审");
        }
        
        // Update fields - preserve weight if not provided
        existing.setScore(assignment.getScore());
        existing.setComment(assignment.getComment());
        
        // Only update weight if explicitly provided in the request
        if (assignment.getWeight() != null) {
            existing.setWeight(assignment.getWeight());
        }
        
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
     * Get all assignments with enriched data (submission title, competition name, description)
     */
    public List<JudgeAssignmentWithDetailsDTO> getAllAssignmentsWithDetails() {
        List<JudgeAssignment> assignments = judgeAssignmentMapper.findAll();
        return enrichAssignments(assignments);
    }
    
    /**
     * Get judge's assignments with enriched data
     */
    public List<JudgeAssignmentWithDetailsDTO> getAssignmentsByJudgeWithDetails(Integer userId) {
        List<JudgeAssignment> assignments = judgeAssignmentMapper.findByUserId(userId);
        return enrichAssignments(assignments);
    }
    
    /**
     * Get submission's assignments with enriched data
     */
    public List<JudgeAssignmentWithDetailsDTO> getAssignmentsBySubmissionWithDetails(Integer submissionId) {
        List<JudgeAssignment> assignments = judgeAssignmentMapper.findBySubmissionId(submissionId);
        return enrichAssignments(assignments);
    }
    
    /**
     * Enrich assignments with related submission, competition, and user data
     * 
     * NOTE: This method has N+1 query performance characteristics. For production use with large datasets,
     * consider implementing a custom MyBatis mapper with JOIN queries to fetch all related data in a single query.
     */
    private List<JudgeAssignmentWithDetailsDTO> enrichAssignments(List<JudgeAssignment> assignments) {
        return assignments.stream()
                .map(assignment -> {
                    JudgeAssignmentWithDetailsDTO dto = JudgeAssignmentWithDetailsDTO.from(assignment);
                    
                    // Get user - handle null case
                    User user = userMapper.findById(assignment.getUserId());
                    if (user != null) {
                        dto.setUser(user);
                    }
                    
                    // Get submission
                    Submission submission = submissionMapper.findById(assignment.getSubmissionId());
                    if (submission != null) {
                        JudgeAssignmentWithDetailsDTO.SubmissionWithDetailsDTO submissionDTO = 
                            JudgeAssignmentWithDetailsDTO.SubmissionWithDetailsDTO.from(submission);
                        
                        // Get registration to get competition
                        if (submission.getRegistrationId() != null) {
                            Registration registration = registrationMapper.findById(submission.getRegistrationId());
                            if (registration != null && registration.getCompetitionId() != null) {
                                Competition competition = competitionMapper.findById(registration.getCompetitionId());
                                submissionDTO.setCompetition(competition);
                            }
                        }
                        
                        dto.setSubmission(submissionDTO);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
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
