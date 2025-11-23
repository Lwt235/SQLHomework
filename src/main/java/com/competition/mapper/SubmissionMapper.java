package com.competition.mapper;

import com.competition.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SubmissionMapper {
    
    List<Submission> findAll();
    
    Submission findById(Integer submissionId);
    
    List<Submission> findByRegistrationId(Integer registrationId);
    
    List<Submission> findBySubmissionStatus(String status);
    
    void insert(Submission submission);
    
    void update(Submission submission);
    
    void deleteById(Integer submissionId);
}
