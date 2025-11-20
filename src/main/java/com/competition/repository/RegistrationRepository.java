package com.competition.repository;

import com.competition.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {
    List<Registration> findByCompetitionId(Integer competitionId);
    List<Registration> findByUserId(Integer userId);
    List<Registration> findByTeamId(Integer teamId);
    List<Registration> findByRegistrationStatus(String status);
}
