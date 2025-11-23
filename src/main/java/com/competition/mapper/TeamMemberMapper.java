package com.competition.mapper;

import com.competition.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TeamMemberMapper {
    
    List<TeamMember> findByTeamId(Integer teamId);
    
    List<TeamMember> findByUserId(Integer userId);
    
    TeamMember findById(@Param("teamId") Integer teamId, @Param("userId") Integer userId);
    
    void insert(TeamMember teamMember);
    
    void update(TeamMember teamMember);
    
    void delete(TeamMember teamMember);
    
    void deleteByTeamId(Integer teamId);
}
