package com.competition.service;

import com.competition.dto.RegistrationWithCompetitionDTO;
import com.competition.entity.Registration;
import com.competition.entity.Competition;
import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.entity.TeamMember;
import com.competition.mapper.RegistrationMapper;
import com.competition.mapper.CompetitionMapper;
import com.competition.mapper.UserMapper;
import com.competition.mapper.UserRoleMapper;
import com.competition.mapper.RoleMapper;
import com.competition.mapper.TeamMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private TeamMemberMapper teamMemberMapper;

    public List<Registration> getAllRegistrations() {
        return registrationMapper.findAll().stream()
                .filter(registration -> !registration.getDeleted())
                .collect(Collectors.toList());
    }

    public Optional<Registration> getRegistrationById(Integer id) {
        return Optional.ofNullable(registrationMapper.findById(id));
    }

    public Registration createRegistration(Registration registration) {
        // Validate competition exists and is not deleted
        Competition competition = competitionMapper.findById(registration.getCompetitionId());
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        if (competition.getDeleted()) {
            throw new RuntimeException("该比赛已被删除，无法报名");
        }
        
        // Validate competition is open for registration
        LocalDateTime now = LocalDateTime.now();
        if (competition.getSignupStart() != null && now.isBefore(competition.getSignupStart())) {
            throw new RuntimeException("报名尚未开始");
        }
        
        if (competition.getSignupEnd() != null && now.isAfter(competition.getSignupEnd())) {
            throw new RuntimeException("报名已结束");
        }
        
        // Individual registration
        if (registration.getUserId() != null && registration.getTeamId() == null) {
            User user = userMapper.findById(registration.getUserId());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            if (user.getDeleted()) {
                throw new RuntimeException("用户账号已被删除");
            }
            
            if (!UserService.STATUS_ACTIVE.equals(user.getUserStatus())) {
                throw new RuntimeException("只有激活状态的用户才能报名参赛");
            }
            
            // Check if user has student role
            List<UserRole> userRoles = userRoleMapper.findByUserId(registration.getUserId());
            boolean isStudent = false;
            for (UserRole userRole : userRoles) {
                Role role = roleMapper.findById(userRole.getRoleId());
                if (role != null && "STUDENT".equalsIgnoreCase(role.getRoleCode())) {
                    isStudent = true;
                    break;
                }
            }
            
            if (!isStudent) {
                throw new RuntimeException("只有学生账号可以报名参赛");
            }
            
            // Check if user already registered for this competition (individual or team)
            if (hasUserRegisteredForCompetition(registration.getUserId(), registration.getCompetitionId())) {
                throw new RuntimeException("您已经报名了该比赛（个人或团队）");
            }
        }
        
        // Team registration
        if (registration.getTeamId() != null) {
            // Get all team members
            List<TeamMember> teamMembers = teamMemberMapper.findByTeamId(registration.getTeamId());
            if (teamMembers.isEmpty()) {
                throw new RuntimeException("团队没有成员，无法报名");
            }
            
            // Check if any team member has already registered for this competition
            for (TeamMember member : teamMembers) {
                if (hasUserRegisteredForCompetition(member.getUserId(), registration.getCompetitionId())) {
                    User user = userMapper.findById(member.getUserId());
                    String username = user != null ? user.getUsername() : "ID:" + member.getUserId();
                    throw new RuntimeException("团队成员 " + username + " 已经报名了该比赛（个人或其他团队），无法重复报名");
                }
            }
        }
        
        registration.setRegistrationStatus("pending");
        registrationMapper.insert(registration);
        return registration;
    }
    
    /**
     * Check if a user has already registered for a competition (individual or as part of a team)
     */
    private boolean hasUserRegisteredForCompetition(Integer userId, Integer competitionId) {
        // Check individual registrations
        List<Registration> individualRegs = registrationRepository
                .findByCompetitionId(competitionId)
                .stream()
                .filter(r -> !r.getDeleted() && r.getUserId() != null && r.getUserId().equals(userId))
                .collect(Collectors.toList());
        
        if (!individualRegs.isEmpty()) {
            return true;
        }
        
        // Check team registrations
        List<TeamMember> userTeams = teamMemberMapper.findByUserId(userId);
        List<Integer> userTeamIds = userTeams.stream()
                .map(TeamMember::getTeamId)
                .collect(Collectors.toList());
        
        List<Registration> teamRegs = registrationRepository
                .findByCompetitionId(competitionId)
                .stream()
                .filter(r -> !r.getDeleted() && r.getTeamId() != null && userTeamIds.contains(r.getTeamId()))
                .collect(Collectors.toList());
        
        return !teamRegs.isEmpty();
    }

    public Registration updateRegistrationStatus(Integer id, String status, Integer auditUserId) {
        Registration registration = registrationMapper.findById(id);
        if (registration == null) {
            throw new RuntimeException("Registration not found");
        }

        registration.setRegistrationStatus(status);
        registration.setAuditUserId(auditUserId);
        registration.setAuditTime(LocalDateTime.now());

        registrationMapper.update(registration);
        return registration;
    }

    public List<Registration> getRegistrationsByCompetition(Integer competitionId) {
        return registrationMapper.findByCompetitionId(competitionId);
    }

    public List<RegistrationWithCompetitionDTO> getRegistrationsByUser(Integer userId) {
        // Get direct user registrations
        List<Registration> directRegistrations = registrationRepository.findByUserId(userId);
        
        // Get team-based registrations where user is a member
        List<TeamMember> teamMemberships = teamMemberMapper.findByUserId(userId);
        List<Integer> teamIds = teamMemberships.stream()
                .map(TeamMember::getTeamId)
                .collect(Collectors.toList());
        
        List<Registration> teamRegistrations = new java.util.ArrayList<>();
        for (Integer teamId : teamIds) {
            List<Registration> regs = registrationRepository.findByTeamId(teamId);
            teamRegistrations.addAll(regs);
        }
        
        // Combine and deduplicate
        java.util.Set<Integer> registrationIds = new java.util.HashSet<>();
        List<Registration> allRegistrations = new java.util.ArrayList<>();
        
        for (Registration reg : directRegistrations) {
            if (!reg.getDeleted() && registrationIds.add(reg.getRegistrationId())) {
                allRegistrations.add(reg);
            }
        }
        
        for (Registration reg : teamRegistrations) {
            if (!reg.getDeleted() && registrationIds.add(reg.getRegistrationId())) {
                allRegistrations.add(reg);
            }
        }
        
        // Map to DTOs with competition details
        return allRegistrations.stream()
                .map(reg -> {
                    Competition competition = competitionMapper.findById(reg.getCompetitionId());
                    return RegistrationWithCompetitionDTO.from(reg, competition);
                })
                .collect(Collectors.toList());
    }

    public List<Registration> getRegistrationsByStatus(String status) {
        return registrationRepository.findByRegistrationStatus(status);
    }
}
