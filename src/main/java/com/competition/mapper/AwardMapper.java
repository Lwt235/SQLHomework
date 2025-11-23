package com.competition.mapper;

import com.competition.entity.Award;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AwardMapper {
    
    @Select("SELECT * FROM Award WHERE is_deleted = FALSE")
    @Results(id = "awardResultMap", value = {
        @Result(property = "awardId", column = "award_id", id = true),
        @Result(property = "competitionId", column = "competition_id"),
        @Result(property = "awardName", column = "award_name"),
        @Result(property = "awardLevel", column = "award_level"),
        @Result(property = "criteriaDescription", column = "criteria_description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<Award> findAll();
    
    @Select("SELECT * FROM Award WHERE award_id = #{awardId} AND is_deleted = FALSE")
    @ResultMap("awardResultMap")
    Award findById(Integer awardId);
    
    @Select("SELECT * FROM Award WHERE competition_id = #{competitionId} AND is_deleted = FALSE")
    @ResultMap("awardResultMap")
    List<Award> findByCompetitionId(Integer competitionId);
    
    @Insert("INSERT INTO Award (competition_id, award_name, award_level, criteria_description, " +
            "created_at, updated_at, is_deleted) " +
            "VALUES (#{competitionId}, #{awardName}, #{awardLevel}, #{criteriaDescription}, NOW(), NOW(), FALSE)")
    @Options(useGeneratedKeys = true, keyProperty = "awardId", keyColumn = "award_id")
    void insert(Award award);
    
    @Update("UPDATE Award SET competition_id = #{competitionId}, award_name = #{awardName}, " +
            "award_level = #{awardLevel}, criteria_description = #{criteriaDescription}, updated_at = NOW() " +
            "WHERE award_id = #{awardId}")
    void update(Award award);
    
    @Update("UPDATE Award SET is_deleted = TRUE WHERE award_id = #{awardId}")
    void deleteById(Integer awardId);
}
