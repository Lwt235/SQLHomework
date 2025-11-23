package com.competition.mapper;

import com.competition.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM User WHERE is_deleted = FALSE")
    @Results(id = "userResultMap", value = {
        @Result(property = "userId", column = "user_id", id = true),
        @Result(property = "userStatus", column = "user_status"),
        @Result(property = "username", column = "username"),
        @Result(property = "passwordHash", column = "password_hash"),
        @Result(property = "realName", column = "real_name"),
        @Result(property = "email", column = "email"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "school", column = "school"),
        @Result(property = "department", column = "department"),
        @Result(property = "studentNo", column = "student_no"),
        @Result(property = "authType", column = "auth_type"),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<User> findAll();
    
    @Select("SELECT * FROM User WHERE user_id = #{userId}")
    @ResultMap("userResultMap")
    User findById(Integer userId);
    
    @Select("SELECT * FROM User WHERE username = #{username}")
    @ResultMap("userResultMap")
    User findByUsername(String username);
    
    @Select("SELECT * FROM User WHERE email = #{email}")
    @ResultMap("userResultMap")
    User findByEmail(String email);
    
    @Select("SELECT COUNT(*) FROM User WHERE username = #{username}")
    boolean existsByUsername(String username);
    
    @Select("SELECT COUNT(*) FROM User WHERE email = #{email}")
    boolean existsByEmail(String email);
    
    @Select("SELECT * FROM User WHERE is_deleted = FALSE AND user_status = 'active' " +
            "AND (LOWER(username) LIKE CONCAT('%', LOWER(#{query}), '%') " +
            "OR CAST(user_id AS CHAR) LIKE CONCAT('%', #{query}, '%'))")
    @ResultMap("userResultMap")
    List<User> searchByUsernameOrId(@Param("query") String query);
    
    @Insert("INSERT INTO User (user_status, username, password_hash, real_name, email, phone, " +
            "school, department, student_no, auth_type, last_login_at, created_at, updated_at, is_deleted) " +
            "VALUES (#{userStatus}, #{username}, #{passwordHash}, #{realName}, #{email}, #{phone}, " +
            "#{school}, #{department}, #{studentNo}, #{authType}, #{lastLoginAt}, NOW(), NOW(), FALSE)")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    void insert(User user);
    
    @Update("UPDATE User SET user_status = #{userStatus}, username = #{username}, " +
            "password_hash = #{passwordHash}, real_name = #{realName}, email = #{email}, " +
            "phone = #{phone}, school = #{school}, department = #{department}, " +
            "student_no = #{studentNo}, auth_type = #{authType}, last_login_at = #{lastLoginAt}, " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    void update(User user);
    
    @Update("UPDATE User SET is_deleted = TRUE WHERE user_id = #{userId}")
    void deleteById(Integer userId);
}
