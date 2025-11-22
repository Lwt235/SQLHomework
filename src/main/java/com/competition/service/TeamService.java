package com.competition.service;

import com.competition.entity.Team;
import com.competition.entity.TeamMember;
import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.repository.TeamRepository;
import com.competition.repository.TeamMemberRepository;
import com.competition.repository.UserRepository;
import com.competition.repository.UserRoleRepository;
import com.competition.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {
    
    // Role constants
    public static final String ROLE_LEADER = "leader";
    public static final String ROLE_VICE_LEADER = "vice-leader";
    public static final String ROLE_MEMBER = "member";
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    public List<Team> getAllTeams() {
        return teamRepository.findAll().stream()
                .filter(team -> !team.getDeleted())
                .collect(Collectors.toList());
    }
    
    public Team getTeamById(Integer teamId) {
        return teamRepository.findById(teamId)
                .filter(team -> !team.getDeleted())
                .orElseThrow(() -> new RuntimeException("团队不存在"));
    }
    
    public List<TeamMember> getTeamMembers(Integer teamId) {
        return teamMemberRepository.findByTeamId(teamId);
    }
    
    public List<Team> getTeamsByUserId(Integer userId) {
        List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(tm -> teamRepository.findById(tm.getTeamId()))
                .filter(opt -> opt.isPresent() && !opt.get().getDeleted())
                .map(opt -> opt.get())
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Team createTeam(Team team, Integer creatorUserId) {
        // Validate team name
        if (team.getTeamName() == null || team.getTeamName().trim().isEmpty()) {
            throw new RuntimeException("团队名称不能为空");
        }
        
        // Validate creator exists
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("创建者不存在"));
        
        if (creator.getDeleted()) {
            throw new RuntimeException("用户账号已被删除");
        }
        
        if (!UserService.STATUS_ACTIVE.equals(creator.getUserStatus())) {
            throw new RuntimeException("只有激活状态的用户才能创建团队");
        }
        
        team.setFormedAt(LocalDateTime.now());
        Team savedTeam = teamRepository.save(team);
        
        // Add creator as team leader
        TeamMember leaderMember = new TeamMember();
        leaderMember.setTeamId(savedTeam.getTeamId());
        leaderMember.setUserId(creatorUserId);
        leaderMember.setRoleInTeam(ROLE_LEADER);
        teamMemberRepository.save(leaderMember);
        
        return savedTeam;
    }
    
    public Team updateTeam(Integer teamId, Team teamUpdate) {
        Team team = getTeamById(teamId);
        
        if (teamUpdate.getTeamName() != null && !teamUpdate.getTeamName().trim().isEmpty()) {
            team.setTeamName(teamUpdate.getTeamName());
        }
        
        if (teamUpdate.getDescription() != null) {
            team.setDescription(teamUpdate.getDescription());
        }
        
        return teamRepository.save(team);
    }
    
    @Transactional
    public void deleteTeam(Integer teamId) {
        Team team = getTeamById(teamId);
        team.setDeleted(true);
        teamRepository.save(team);
    }
    
    @Transactional
    public void addTeamMember(Integer teamId, Integer userId, String roleInTeam) {
        // Validate team exists
        Team team = getTeamById(teamId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getDeleted()) {
            throw new RuntimeException("用户账号已被删除");
        }
        
        if (!UserService.STATUS_ACTIVE.equals(user.getUserStatus())) {
            throw new RuntimeException("只有激活状态的用户才能加入团队");
        }
        
        // Check if user is already in team
        List<TeamMember> existingMembers = teamMemberRepository.findByTeamId(teamId);
        boolean alreadyMember = existingMembers.stream()
                .anyMatch(tm -> tm.getUserId().equals(userId));
        
        if (alreadyMember) {
            throw new RuntimeException("该用户已经是团队成员");
        }
        
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(teamId);
        teamMember.setUserId(userId);
        teamMember.setRoleInTeam(roleInTeam != null ? roleInTeam : ROLE_MEMBER);
        teamMemberRepository.save(teamMember);
    }
    
    @Transactional
    public void removeTeamMember(Integer teamId, Integer userId) {
        Team team = getTeamById(teamId);
        
        TeamMember.TeamMemberId id = new TeamMember.TeamMemberId();
        id.setTeamId(teamId);
        id.setUserId(userId);
        
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        
        // Don't allow removing the leader if there are other members
        if (ROLE_LEADER.equals(member.getRoleInTeam())) {
            List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
            if (members.size() > 1) {
                throw new RuntimeException("团队队长不能在有其他成员时退出，请先转让队长或解散团队");
            }
        }
        
        teamMemberRepository.delete(member);
    }
    
    public List<com.competition.dto.TeamMemberDetailDTO> getTeamMembersWithDetails(Integer teamId, Integer currentUserId) {
        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        List<com.competition.dto.TeamMemberDetailDTO> result = new java.util.ArrayList<>();
        
        // Get all user IDs to fetch in one query
        List<Integer> userIds = members.stream()
                .map(TeamMember::getUserId)
                .collect(Collectors.toList());
        
        // Fetch all users at once to avoid N+1 queries
        List<User> users = userRepository.findAllById(userIds);
        java.util.Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        
        for (TeamMember member : members) {
            com.competition.dto.TeamMemberDetailDTO dto = new com.competition.dto.TeamMemberDetailDTO();
            dto.setUserId(member.getUserId());
            dto.setTeamId(member.getTeamId());
            dto.setRoleInTeam(member.getRoleInTeam());
            
            // Get user details from map
            User user = userMap.get(member.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername()); // username is the nickname
                dto.setRealName(user.getRealName());
            }
            
            // Mark if this is the current user
            dto.setIsCurrentUser(currentUserId != null && currentUserId.equals(member.getUserId()));
            
            result.add(dto);
        }
        
        return result;
    }
    
    @Transactional
    public void transferCaptain(Integer teamId, Integer currentCaptainUserId, Integer newCaptainUserId) {
        // Validate team exists
        Team team = getTeamById(teamId);
        
        // Check that current user is the captain
        TeamMember.TeamMemberId currentCaptainId = new TeamMember.TeamMemberId();
        currentCaptainId.setTeamId(teamId);
        currentCaptainId.setUserId(currentCaptainUserId);
        
        TeamMember currentCaptain = teamMemberRepository.findById(currentCaptainId)
                .orElseThrow(() -> new RuntimeException("当前用户不是团队成员"));
        
        if (!ROLE_LEADER.equals(currentCaptain.getRoleInTeam())) {
            throw new RuntimeException("只有队长才能转让队长身份");
        }
        
        // Check that new captain is a member of the team
        TeamMember.TeamMemberId newCaptainId = new TeamMember.TeamMemberId();
        newCaptainId.setTeamId(teamId);
        newCaptainId.setUserId(newCaptainUserId);
        
        TeamMember newCaptain = teamMemberRepository.findById(newCaptainId)
                .orElseThrow(() -> new RuntimeException("目标用户不是团队成员"));
        
        // Transfer captain role
        currentCaptain.setRoleInTeam(ROLE_MEMBER);
        newCaptain.setRoleInTeam(ROLE_LEADER);
        
        teamMemberRepository.save(currentCaptain);
        teamMemberRepository.save(newCaptain);
    }
    
    /**
     * Update team member role (leader can update any member's role)
     */
    @Transactional
    public void updateMemberRole(Integer teamId, Integer operatorUserId, Integer targetUserId, String newRole) {
        // Validate team exists
        Team team = getTeamById(teamId);
        
        // Check that operator is the captain
        TeamMember.TeamMemberId operatorId = new TeamMember.TeamMemberId();
        operatorId.setTeamId(teamId);
        operatorId.setUserId(operatorUserId);
        
        TeamMember operator = teamMemberRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("您不是团队成员"));
        
        if (!ROLE_LEADER.equals(operator.getRoleInTeam())) {
            throw new RuntimeException("只有队长才能修改成员角色");
        }
        
        // Get target member
        TeamMember.TeamMemberId targetId = new TeamMember.TeamMemberId();
        targetId.setTeamId(teamId);
        targetId.setUserId(targetUserId);
        
        TeamMember targetMember = teamMemberRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("目标用户不是团队成员"));
        
        // Validate new role
        if (!newRole.equals(ROLE_LEADER) && !newRole.equals(ROLE_VICE_LEADER) && !newRole.equals(ROLE_MEMBER)) {
            throw new RuntimeException("无效的角色类型");
        }
        
        // If promoting to leader, demote current leader
        if (ROLE_LEADER.equals(newRole)) {
            // Find current leader(s) and demote them to member
            List<TeamMember> allMembers = teamMemberRepository.findByTeamId(teamId);
            for (TeamMember member : allMembers) {
                if (ROLE_LEADER.equals(member.getRoleInTeam()) && !member.getUserId().equals(targetUserId)) {
                    member.setRoleInTeam(ROLE_MEMBER);
                    teamMemberRepository.save(member);
                }
            }
        }
        
        // Update target member role
        targetMember.setRoleInTeam(newRole);
        teamMemberRepository.save(targetMember);
    }
    
    public List<User> searchUsersByNicknameOrUsername(String query) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // Search by username or ID
        List<User> users = userRepository.searchByUsernameOrId(query.trim());
        
        // Filter to only include students
        return users.stream()
                .filter(user -> hasStudentRole(user.getUserId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if user has STUDENT role
     */
    private boolean hasStudentRole(Integer userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        
        for (UserRole userRole : userRoles) {
            Role role = roleRepository.findById(userRole.getRoleId()).orElse(null);
            if (role != null && "STUDENT".equalsIgnoreCase(role.getRoleCode())) {
                return true;
            }
        }
        
        return false;
    }
    
    public List<Team> getTeamsByUserIdAndRole(Integer userId, String role) {
        List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);
        return memberships.stream()
                .filter(tm -> role.equals(tm.getRoleInTeam()))
                .map(tm -> teamRepository.findById(tm.getTeamId()))
                .filter(opt -> opt.isPresent() && !opt.get().getDeleted())
                .map(opt -> opt.get())
                .collect(Collectors.toList());
    }
}
