package com.competition.mapper;

import com.competition.entity.Registration;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RegistrationMapper {
    
    @Select("SELECT * FROM Registration WHERE is_deleted = FALSE")
    @Results(id = "registrationResultMap", value = {
        @Result(property = "registrationId", column = "registration_id", id = true),
        @Result(property = "competitionId", column = "competition_id"),
        @Result(property = "teamId", column = "team_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "auditUserId", column = "audit_user_id"),
        @Result(property = "registrationStatus", column = "registration_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "remark", column = "remark"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<Registration> findAll();
    
    @Select("SELECT * FROM Registration WHERE registration_id = #{registrationId} AND is_deleted = FALSE")
    @ResultMap("registrationResultMap")
    Registration findById(Integer registrationId);
    
    @Select("SELECT * FROM Registration WHERE competition_id = #{competitionId} AND is_deleted = FALSE")
    @ResultMap("registrationResultMap")
    List<Registration> findByCompetitionId(Integer competitionId);
    
    @Insert("INSERT INTO Registration (competition_id, team_id, user_id, audit_user_id, registration_status, " +
            "audit_time, remark, created_at, updated_at, is_deleted) " +
            "VALUES (#{competitionId}, #{teamId}, #{userId}, #{auditUserId}, #{registrationStatus}, " +
            "#{auditTime}, #{remark}, NOW(), NOW(), FALSE)")
    @Options(useGeneratedKeys = true, keyProperty = "registrationId", keyColumn = "registration_id")
    void insert(Registration registration);
    
    @Update("UPDATE Registration SET competition_id = #{competitionId}, team_id = #{teamId}, " +
            "user_id = #{userId}, audit_user_id = #{auditUserId}, registration_status = #{registrationStatus}, " +
            "audit_time = #{auditTime}, remark = #{remark}, updated_at = NOW() " +
            "WHERE registration_id = #{registrationId}")
    void update(Registration registration);
    
    @Update("UPDATE Registration SET is_deleted = TRUE WHERE registration_id = #{registrationId}")
    void deleteById(Integer registrationId);
}
