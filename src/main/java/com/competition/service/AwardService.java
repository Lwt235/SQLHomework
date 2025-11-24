package com.competition.service;

import com.competition.entity.Award;
import com.competition.entity.AwardResult;
import com.competition.mapper.AwardMapper;
import com.competition.mapper.AwardResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AwardService {

    @Autowired
    private AwardMapper awardMapper;

    @Autowired
    private AwardResultMapper awardResultMapper;

    public List<Award> getAllAwards() {
        return awardMapper.findAll();
    }

    public Optional<Award> getAwardById(Integer id) {
        return Optional.ofNullable(awardMapper.findById(id));
    }

    public Award createAward(Award award) {
        awardMapper.insert(award);
        return award;
    }

    public Award updateAward(Integer id, Award awardDetails) {
        Award award = awardMapper.findById(id);
        if (award == null) {
            throw new RuntimeException("Award not found");
        }

        award.setAwardName(awardDetails.getAwardName());
        award.setAwardLevel(awardDetails.getAwardLevel());
        award.setAwardPercentage(awardDetails.getAwardPercentage());
        award.setPriority(awardDetails.getPriority());
        award.setCriteriaDescription(awardDetails.getCriteriaDescription());

        awardMapper.update(award);
        return award;
    }

    public List<Award> getAwardsByCompetition(Integer competitionId) {
        return awardMapper.findByCompetitionId(competitionId);
    }

    public AwardResult grantAward(Integer registrationId, Integer awardId, String certificateNo) {
        AwardResult result = new AwardResult();
        result.setRegistrationId(registrationId);
        result.setAwardId(awardId);
        result.setAwardTime(LocalDateTime.now());
        result.setCertificateNo(certificateNo);

        awardResultMapper.insert(result);
        return result;
    }

    public List<AwardResult> getAwardResultsByRegistration(Integer registrationId) {
        return awardResultMapper.findByRegistrationId(registrationId);
    }

    public List<AwardResult> getAwardResultsByAward(Integer awardId) {
        return awardResultMapper.findByAwardId(awardId);
    }
}
