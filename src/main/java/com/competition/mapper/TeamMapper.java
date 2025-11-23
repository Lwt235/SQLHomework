package com.competition.mapper;

import com.competition.entity.Team;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TeamMapper {
    
    @Select("SELECT * FROM Team WHERE is_deleted = FALSE")
    @Results(id = "teamResultMap", value = {
        @Result(property = "teamId", column = "team_id", id = true),
        @Result(property = "teamName", column = "team_name"),
        @Result(property = "formedAt", column = "formed_at"),
        @Result(property = "description", column = "description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<Team> findAll();
    
    @Select("SELECT * FROM Team WHERE team_id = #{teamId} AND is_deleted = FALSE")
    @ResultMap("teamResultMap")
    Team findById(Integer teamId);
    
    @Insert("INSERT INTO Team (team_name, formed_at, description, created_at, updated_at, is_deleted) " +
            "VALUES (#{teamName}, #{formedAt}, #{description}, NOW(), NOW(), FALSE)")
    @Options(useGeneratedKeys = true, keyProperty = "teamId", keyColumn = "team_id")
    void insert(Team team);
    
    @Update("UPDATE Team SET team_name = #{teamName}, formed_at = #{formedAt}, " +
            "description = #{description}, updated_at = NOW() WHERE team_id = #{teamId}")
    void update(Team team);
    
    @Update("UPDATE Team SET is_deleted = TRUE WHERE team_id = #{teamId}")
    void deleteById(Integer teamId);
}
