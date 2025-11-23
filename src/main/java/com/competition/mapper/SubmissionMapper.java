package com.competition.mapper;

import com.competition.entity.Submission;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SubmissionMapper {
    
    @Select("SELECT * FROM Submission WHERE is_deleted = FALSE")
    @Results(id = "submissionResultMap", value = {
        @Result(property = "submissionId", column = "submission_id", id = true),
        @Result(property = "registrationId", column = "registration_id"),
        @Result(property = "submissionTitle", column = "submission_title"),
        @Result(property = "abstractText", column = "abstract"),
        @Result(property = "submissionStatus", column = "submission_status"),
        @Result(property = "submittedAt", column = "submitted_at"),
        @Result(property = "finalLockedAt", column = "final_locked_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<Submission> findAll();
    
    @Select("SELECT * FROM Submission WHERE submission_id = #{submissionId} AND is_deleted = FALSE")
    @ResultMap("submissionResultMap")
    Submission findById(Integer submissionId);
    
    @Select("SELECT * FROM Submission WHERE registration_id = #{registrationId} AND is_deleted = FALSE")
    @ResultMap("submissionResultMap")
    List<Submission> findByRegistrationId(Integer registrationId);
    
    @Insert("INSERT INTO Submission (registration_id, submission_title, abstract, submission_status, " +
            "submitted_at, final_locked_at, created_at, updated_at, is_deleted) " +
            "VALUES (#{registrationId}, #{submissionTitle}, #{abstractText}, #{submissionStatus}, " +
            "#{submittedAt}, #{finalLockedAt}, NOW(), NOW(), FALSE)")
    @Options(useGeneratedKeys = true, keyProperty = "submissionId", keyColumn = "submission_id")
    void insert(Submission submission);
    
    @Update("UPDATE Submission SET registration_id = #{registrationId}, submission_title = #{submissionTitle}, " +
            "abstract = #{abstractText}, submission_status = #{submissionStatus}, submitted_at = #{submittedAt}, " +
            "final_locked_at = #{finalLockedAt}, updated_at = NOW() WHERE submission_id = #{submissionId}")
    void update(Submission submission);
    
    @Update("UPDATE Submission SET is_deleted = TRUE WHERE submission_id = #{submissionId}")
    void deleteById(Integer submissionId);
}
