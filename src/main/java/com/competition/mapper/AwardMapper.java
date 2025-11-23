package com.competition.mapper;

import com.competition.entity.Award;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AwardMapper {
    
    List<Award> findAll();
    
    Award findById(Integer awardId);
    
    List<Award> findByCompetitionId(Integer competitionId);
    
    void insert(Award award);
    
    void update(Award award);
    
    void deleteById(Integer awardId);
}
