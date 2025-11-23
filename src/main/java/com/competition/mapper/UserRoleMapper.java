package com.competition.mapper;

import com.competition.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserRoleMapper {
    
    List<UserRole> findByUserId(Integer userId);
    
    List<UserRole> findByRoleId(Integer roleId);
    
    void insert(UserRole userRole);
    
    void delete(UserRole userRole);
    
    void deleteByUserId(Integer userId);
}
