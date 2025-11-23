package com.competition.mapper;

import com.competition.entity.Role;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RoleMapper {
    
    @Select("SELECT * FROM Role WHERE is_deleted = FALSE")
    @Results(id = "roleResultMap", value = {
        @Result(property = "roleId", column = "role_id", id = true),
        @Result(property = "roleCode", column = "role_code"),
        @Result(property = "roleName", column = "role_name"),
        @Result(property = "description", column = "description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<Role> findAll();
    
    @Select("SELECT * FROM Role WHERE role_id = #{roleId} AND is_deleted = FALSE")
    @ResultMap("roleResultMap")
    Role findById(Integer roleId);
    
    @Select("SELECT * FROM Role WHERE role_id IN (#{ids}) AND is_deleted = FALSE")
    @ResultMap("roleResultMap")
    List<Role> findAllById(@Param("ids") List<Integer> ids);
}
