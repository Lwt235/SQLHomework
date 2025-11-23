package com.competition.mapper;

import com.competition.entity.AwardResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AwardResultMapper {
    
    List<AwardResult> findAll();
    
    AwardResult findById(@Param("registrationId") Integer registrationId, @Param("awardId") Integer awardId);
    
    List<AwardResult> findByRegistrationId(Integer registrationId);
    
    List<AwardResult> findByAwardId(Integer awardId);
    
    void insert(AwardResult awardResult);
    
    void update(AwardResult awardResult);
    
    void deleteById(@Param("registrationId") Integer registrationId, @Param("awardId") Integer awardId);
}
