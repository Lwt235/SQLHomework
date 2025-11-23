package com.competition.mapper;

import com.competition.entity.AwardResult;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AwardResultMapper {
    
    @Select("SELECT * FROM AwardResult WHERE is_deleted = FALSE")
    @Results(id = "awardResultResultMap", value = {
        @Result(property = "registrationId", column = "registration_id"),
        @Result(property = "awardId", column = "award_id"),
        @Result(property = "awardTime", column = "award_time"),
        @Result(property = "certificateNo", column = "certificate_no"),
        @Result(property = "certificatePath", column = "certificate_path"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<AwardResult> findAll();
    
    @Select("SELECT * FROM AwardResult WHERE registration_id = #{registrationId} AND award_id = #{awardId} AND is_deleted = FALSE")
    @ResultMap("awardResultResultMap")
    AwardResult findById(@Param("registrationId") Integer registrationId, @Param("awardId") Integer awardId);
    
    @Select("SELECT * FROM AwardResult WHERE registration_id = #{registrationId} AND is_deleted = FALSE")
    @ResultMap("awardResultResultMap")
    List<AwardResult> findByRegistrationId(Integer registrationId);
    
    @Insert("INSERT INTO AwardResult (registration_id, award_id, award_time, certificate_no, certificate_path, " +
            "created_at, updated_at, is_deleted) " +
            "VALUES (#{registrationId}, #{awardId}, #{awardTime}, #{certificateNo}, #{certificatePath}, NOW(), NOW(), FALSE)")
    void insert(AwardResult awardResult);
    
    @Update("UPDATE AwardResult SET award_time = #{awardTime}, certificate_no = #{certificateNo}, " +
            "certificate_path = #{certificatePath}, updated_at = NOW() " +
            "WHERE registration_id = #{registrationId} AND award_id = #{awardId}")
    void update(AwardResult awardResult);
    
    @Update("UPDATE AwardResult SET is_deleted = TRUE WHERE registration_id = #{registrationId} AND award_id = #{awardId}")
    void deleteById(@Param("registrationId") Integer registrationId, @Param("awardId") Integer awardId);
}
