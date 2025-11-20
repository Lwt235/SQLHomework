package com.competition.repository;

import com.competition.entity.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AwardRepository extends JpaRepository<Award, Integer> {
    List<Award> findByCompetitionId(Integer competitionId);
    List<Award> findByAwardLevel(String level);
}
