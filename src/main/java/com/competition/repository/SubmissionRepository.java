package com.competition.repository;

import com.competition.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByRegistrationId(Integer registrationId);
    List<Submission> findBySubmissionStatus(String status);
}
