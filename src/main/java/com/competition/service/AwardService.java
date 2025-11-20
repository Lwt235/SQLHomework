package com.competition.service;

import com.competition.entity.Award;
import com.competition.entity.AwardResult;
import com.competition.repository.AwardRepository;
import com.competition.repository.AwardResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AwardService {

    @Autowired
    private AwardRepository awardRepository;

    @Autowired
    private AwardResultRepository awardResultRepository;

    public List<Award> getAllAwards() {
        return awardRepository.findAll();
    }

    public Optional<Award> getAwardById(Integer id) {
        return awardRepository.findById(id);
    }

    public Award createAward(Award award) {
        return awardRepository.save(award);
    }

    public Award updateAward(Integer id, Award awardDetails) {
        Award award = awardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Award not found"));

        award.setAwardName(awardDetails.getAwardName());
        award.setAwardLevel(awardDetails.getAwardLevel());
        award.setCriteriaDescription(awardDetails.getCriteriaDescription());

        return awardRepository.save(award);
    }

    public List<Award> getAwardsByCompetition(Integer competitionId) {
        return awardRepository.findByCompetitionId(competitionId);
    }

    public AwardResult grantAward(Integer registrationId, Integer awardId, String certificateNo) {
        AwardResult result = new AwardResult();
        result.setRegistrationId(registrationId);
        result.setAwardId(awardId);
        result.setAwardTime(LocalDateTime.now());
        result.setCertificateNo(certificateNo);

        return awardResultRepository.save(result);
    }

    public List<AwardResult> getAwardResultsByRegistration(Integer registrationId) {
        return awardResultRepository.findByRegistrationId(registrationId);
    }

    public List<AwardResult> getAwardResultsByAward(Integer awardId) {
        return awardResultRepository.findByAwardId(awardId);
    }
}
