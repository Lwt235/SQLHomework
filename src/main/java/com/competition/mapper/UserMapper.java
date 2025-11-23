package com.competition.mapper;

import com.competition.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    
    List<User> findAll();
    
    User findById(Integer userId);
    
    User findByUsername(String username);
    
    User findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> searchByUsernameOrId(@Param("query") String query);
    
    void insert(User user);
    
    void update(User user);
    
    void deleteById(Integer userId);
}
