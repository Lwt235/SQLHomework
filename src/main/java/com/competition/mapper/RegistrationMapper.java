package com.competition.mapper;

import com.competition.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface RegistrationMapper {
    
    List<Registration> findAll();
    
    Registration findById(Integer registrationId);
    
    List<Registration> findByCompetitionId(Integer competitionId);
    
    List<Registration> findByUserId(Integer userId);
    
    List<Registration> findByTeamId(Integer teamId);
    
    List<Registration> findByRegistrationStatus(String status);
    
    void insert(Registration registration);
    
    void update(Registration registration);
    
    void deleteById(Integer registrationId);
}
