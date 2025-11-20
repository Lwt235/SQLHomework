package com.competition.repository;

import com.competition.entity.JudgeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JudgeAssignmentRepository extends JpaRepository<JudgeAssignment, JudgeAssignment.JudgeAssignmentId> {
    List<JudgeAssignment> findByUserId(Integer userId);
    List<JudgeAssignment> findBySubmissionId(Integer submissionId);
}
