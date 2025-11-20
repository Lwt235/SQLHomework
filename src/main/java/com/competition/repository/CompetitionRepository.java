package com.competition.repository;

import com.competition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Integer> {
    List<Competition> findByCompetitionStatus(String status);
    List<Competition> findByCategory(String category);
    List<Competition> findByLevel(String level);
}
