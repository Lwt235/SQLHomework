package com.competition.mapper;

import com.competition.entity.Competition;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CompetitionMapper {
    
    @Select("SELECT * FROM Competition WHERE is_deleted = FALSE")
    @Results(id = "competitionResultMap", value = {
        @Result(property = "competitionId", column = "competition_id", id = true),
        @Result(property = "competitionTitle", column = "competition_title"),
        @Result(property = "shortTitle", column = "short_title"),
        @Result(property = "competitionStatus", column = "competition_status"),
        @Result(property = "description", column = "description"),
        @Result(property = "category", column = "category"),
        @Result(property = "level", column = "level"),
        @Result(property = "organizer", column = "organizer"),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "endDate", column = "end_date"),
        @Result(property = "signupStart", column = "signup_start"),
        @Result(property = "signupEnd", column = "signup_end"),
        @Result(property = "submitStart", column = "submit_start"),
        @Result(property = "submitEnd", column = "submit_end"),
        @Result(property = "reviewStart", column = "review_start"),
        @Result(property = "reviewEnd", column = "review_end"),
        @Result(property = "awardPublishDate", column = "award_publish_date"),
        @Result(property = "maxTeamSize", column = "max_team_size"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<Competition> findAll();
    
    @Select("SELECT * FROM Competition WHERE competition_id = #{competitionId} AND is_deleted = FALSE")
    @ResultMap("competitionResultMap")
    Competition findById(Integer competitionId);
    
    @Select("SELECT * FROM Competition WHERE competition_status = #{status} AND is_deleted = FALSE")
    @ResultMap("competitionResultMap")
    List<Competition> findByCompetitionStatus(String status);
    
    @Select("SELECT * FROM Competition WHERE category = #{category} AND is_deleted = FALSE")
    @ResultMap("competitionResultMap")
    List<Competition> findByCategory(String category);
    
    @Select("SELECT * FROM Competition WHERE level = #{level} AND is_deleted = FALSE")
    @ResultMap("competitionResultMap")
    List<Competition> findByLevel(String level);
    
    @Insert("INSERT INTO Competition (competition_title, short_title, competition_status, description, " +
            "category, level, organizer, start_date, end_date, signup_start, signup_end, submit_start, " +
            "submit_end, review_start, review_end, award_publish_date, max_team_size, created_at, updated_at, is_deleted) " +
            "VALUES (#{competitionTitle}, #{shortTitle}, #{competitionStatus}, #{description}, #{category}, " +
            "#{level}, #{organizer}, #{startDate}, #{endDate}, #{signupStart}, #{signupEnd}, #{submitStart}, " +
            "#{submitEnd}, #{reviewStart}, #{reviewEnd}, #{awardPublishDate}, #{maxTeamSize}, NOW(), NOW(), FALSE)")
    @Options(useGeneratedKeys = true, keyProperty = "competitionId", keyColumn = "competition_id")
    void insert(Competition competition);
    
    @Update("UPDATE Competition SET competition_title = #{competitionTitle}, short_title = #{shortTitle}, " +
            "competition_status = #{competitionStatus}, description = #{description}, category = #{category}, " +
            "level = #{level}, organizer = #{organizer}, start_date = #{startDate}, end_date = #{endDate}, " +
            "signup_start = #{signupStart}, signup_end = #{signupEnd}, submit_start = #{submitStart}, " +
            "submit_end = #{submitEnd}, review_start = #{reviewStart}, review_end = #{reviewEnd}, " +
            "award_publish_date = #{awardPublishDate}, max_team_size = #{maxTeamSize}, updated_at = NOW() " +
            "WHERE competition_id = #{competitionId}")
    void update(Competition competition);
    
    @Update("UPDATE Competition SET is_deleted = TRUE WHERE competition_id = #{competitionId}")
    void deleteById(Integer competitionId);
}
