package com.competition.mapper;

import com.competition.entity.UserRole;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserRoleMapper {
    
    @Select("SELECT * FROM UserRole WHERE user_id = #{userId}")
    @Results(id = "userRoleResultMap", value = {
        @Result(property = "userId", column = "user_id"),
        @Result(property = "roleId", column = "role_id")
    })
    List<UserRole> findByUserId(Integer userId);
    
    @Select("SELECT * FROM UserRole WHERE role_id = #{roleId}")
    @ResultMap("userRoleResultMap")
    List<UserRole> findByRoleId(Integer roleId);
    
    @Insert("INSERT INTO UserRole (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insert(UserRole userRole);
    
    @Delete("DELETE FROM UserRole WHERE user_id = #{userId} AND role_id = #{roleId}")
    void delete(UserRole userRole);
    
    @Delete("DELETE FROM UserRole WHERE user_id = #{userId}")
    void deleteByUserId(Integer userId);
}
