package com.competition.security;

import com.competition.entity.User;
import com.competition.entity.UserRole;
import com.competition.entity.Role;
import com.competition.repository.UserRepository;
import com.competition.repository.UserRoleRepository;
import com.competition.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Load user roles from database and convert to GrantedAuthority objects
        // Spring Security's hasRole() automatically adds "ROLE_" prefix, so we need to add it here too
        List<Integer> roleIds = userRoleRepository.findByUserId(user.getUserId())
                .stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        List<GrantedAuthority> authorities = roleIds.isEmpty() 
                ? new ArrayList<>()
                : roleRepository.findAllById(roleIds).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode().toUpperCase()))
                        .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }
}
