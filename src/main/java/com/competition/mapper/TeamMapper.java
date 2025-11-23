package com.competition.mapper;

import com.competition.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface TeamMapper {
    
    List<Team> findAll();
    
    Team findById(Integer teamId);
    
    void insert(Team team);
    
    void update(Team team);
    
    void deleteById(Integer teamId);
}
