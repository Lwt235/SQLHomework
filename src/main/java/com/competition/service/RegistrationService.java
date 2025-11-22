package com.competition.service;

import com.competition.entity.Registration;
import com.competition.entity.Competition;
import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.entity.TeamMember;
import com.competition.repository.RegistrationRepository;
import com.competition.repository.CompetitionRepository;
import com.competition.repository.UserRepository;
import com.competition.repository.UserRoleRepository;
import com.competition.repository.RoleRepository;
import com.competition.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll().stream()
                .filter(registration -> !registration.getDeleted())
                .collect(Collectors.toList());
    }

    public Optional<Registration> getRegistrationById(Integer id) {
        return registrationRepository.findById(id);
    }

    public Registration createRegistration(Registration registration) {
        // Validate competition exists and is not deleted
        Competition competition = competitionRepository.findById(registration.getCompetitionId())
                .orElseThrow(() -> new RuntimeException("比赛不存在"));
        
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
        
        // Validate user exists and is active
        if (registration.getUserId() != null) {
            User user = userRepository.findById(registration.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (user.getDeleted()) {
                throw new RuntimeException("用户账号已被删除");
            }
            
            if (!UserService.STATUS_ACTIVE.equals(user.getUserStatus())) {
                throw new RuntimeException("只有激活状态的用户才能报名参赛");
            }
            
            // Check if user has student role
            List<UserRole> userRoles = userRoleRepository.findByUserId(registration.getUserId());
            boolean isStudent = false;
            for (UserRole userRole : userRoles) {
                Optional<Role> role = roleRepository.findById(userRole.getRoleId());
                if (role.isPresent() && "STUDENT".equalsIgnoreCase(role.get().getRoleCode())) {
                    isStudent = true;
                    break;
                }
            }
            
            if (!isStudent) {
                throw new RuntimeException("只有学生账号可以报名参赛");
            }
            
            // Check if user already registered for this competition
            List<Registration> existingRegistrations = registrationRepository
                    .findByCompetitionId(registration.getCompetitionId())
                    .stream()
                    .filter(r -> !r.getDeleted() && r.getUserId() != null && r.getUserId().equals(registration.getUserId()))
                    .collect(Collectors.toList());
            
            if (!existingRegistrations.isEmpty()) {
                throw new RuntimeException("您已经报名了该比赛");
            }
        }
        
        registration.setRegistrationStatus("pending");
        return registrationRepository.save(registration);
    }

    public Registration updateRegistrationStatus(Integer id, String status, Integer auditUserId) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        registration.setRegistrationStatus(status);
        registration.setAuditUserId(auditUserId);
        registration.setAuditTime(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    public List<Registration> getRegistrationsByCompetition(Integer competitionId) {
        return registrationRepository.findByCompetitionId(competitionId);
    }

    public List<Registration> getRegistrationsByUser(Integer userId) {
        // Get direct user registrations
        List<Registration> directRegistrations = registrationRepository.findByUserId(userId);
        
        // Get team-based registrations where user is a member
        List<TeamMember> teamMemberships = teamMemberRepository.findByUserId(userId);
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
        
        return allRegistrations;
    }

    public List<Registration> getRegistrationsByStatus(String status) {
        return registrationRepository.findByRegistrationStatus(status);
    }
}
