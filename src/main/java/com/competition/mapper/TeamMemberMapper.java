package com.competition.mapper;

import com.competition.entity.TeamMember;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TeamMemberMapper {
    
    @Select("SELECT * FROM TeamMember WHERE team_id = #{teamId}")
    @Results(id = "teamMemberResultMap", value = {
        @Result(property = "userId", column = "user_id"),
        @Result(property = "teamId", column = "team_id"),
        @Result(property = "roleInTeam", column = "role_in_team")
    })
    List<TeamMember> findByTeamId(Integer teamId);
    
    @Select("SELECT * FROM TeamMember WHERE user_id = #{userId}")
    @ResultMap("teamMemberResultMap")
    List<TeamMember> findByUserId(Integer userId);
    
    @Insert("INSERT INTO TeamMember (user_id, team_id, role_in_team) VALUES (#{userId}, #{teamId}, #{roleInTeam})")
    void insert(TeamMember teamMember);
    
    @Delete("DELETE FROM TeamMember WHERE user_id = #{userId} AND team_id = #{teamId}")
    void delete(TeamMember teamMember);
    
    @Delete("DELETE FROM TeamMember WHERE team_id = #{teamId}")
    void deleteByTeamId(Integer teamId);
}
