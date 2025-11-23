package com.competition.mapper;

import com.competition.entity.JudgeAssignment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface JudgeAssignmentMapper {
    
    @Select("SELECT * FROM JudgeAssignment WHERE is_deleted = FALSE")
    @Results(id = "judgeAssignmentResultMap", value = {
        @Result(property = "userId", column = "user_id"),
        @Result(property = "submissionId", column = "submission_id"),
        @Result(property = "weight", column = "weight"),
        @Result(property = "score", column = "score"),
        @Result(property = "comment", column = "comment"),
        @Result(property = "judgeStatus", column = "judge_status"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<JudgeAssignment> findAll();
    
    @Select("SELECT * FROM JudgeAssignment WHERE user_id = #{userId} AND submission_id = #{submissionId} AND is_deleted = FALSE")
    @ResultMap("judgeAssignmentResultMap")
    JudgeAssignment findById(@Param("userId") Integer userId, @Param("submissionId") Integer submissionId);
    
    @Select("SELECT * FROM JudgeAssignment WHERE submission_id = #{submissionId} AND is_deleted = FALSE")
    @ResultMap("judgeAssignmentResultMap")
    List<JudgeAssignment> findBySubmissionId(Integer submissionId);
    
    @Select("SELECT * FROM JudgeAssignment WHERE user_id = #{userId} AND is_deleted = FALSE")
    @ResultMap("judgeAssignmentResultMap")
    List<JudgeAssignment> findByUserId(Integer userId);
    
    @Insert("INSERT INTO JudgeAssignment (user_id, submission_id, weight, score, comment, judge_status, " +
            "created_at, updated_at, is_deleted) " +
            "VALUES (#{userId}, #{submissionId}, #{weight}, #{score}, #{comment}, #{judgeStatus}, NOW(), NOW(), FALSE)")
    void insert(JudgeAssignment judgeAssignment);
    
    @Update("UPDATE JudgeAssignment SET weight = #{weight}, score = #{score}, comment = #{comment}, " +
            "judge_status = #{judgeStatus}, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND submission_id = #{submissionId}")
    void update(JudgeAssignment judgeAssignment);
    
    @Update("UPDATE JudgeAssignment SET is_deleted = TRUE WHERE user_id = #{userId} AND submission_id = #{submissionId}")
    void deleteById(@Param("userId") Integer userId, @Param("submissionId") Integer submissionId);
}
