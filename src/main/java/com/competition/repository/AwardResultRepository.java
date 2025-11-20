package com.competition.repository;

import com.competition.entity.AwardResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AwardResultRepository extends JpaRepository<AwardResult, AwardResult.AwardResultId> {
    List<AwardResult> findByRegistrationId(Integer registrationId);
    List<AwardResult> findByAwardId(Integer awardId);
}
