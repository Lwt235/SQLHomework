package com.competition.mapper;

import com.competition.entity.JudgeAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface JudgeAssignmentMapper {
    
    List<JudgeAssignment> findAll();
    
    JudgeAssignment findById(@Param("userId") Integer userId, @Param("submissionId") Integer submissionId);
    
    List<JudgeAssignment> findBySubmissionId(Integer submissionId);
    
    List<JudgeAssignment> findByUserId(Integer userId);
    
    void insert(JudgeAssignment judgeAssignment);
    
    void update(JudgeAssignment judgeAssignment);
    
    void deleteById(@Param("userId") Integer userId, @Param("submissionId") Integer submissionId);
}
