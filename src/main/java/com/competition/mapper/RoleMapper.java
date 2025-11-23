package com.competition.mapper;

import com.competition.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMapper {
    
    List<Role> findAll();
    
    Role findById(Integer roleId);
    
    List<Role> findAllById(@Param("ids") List<Integer> ids);
}
