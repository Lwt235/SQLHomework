package com.competition.mapper;

import com.competition.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotificationMapper {
    
    List<Notification> findAll();
    
    Notification findById(Integer notificationId);
    
    List<Notification> findByUserIdAndDeletedOrderByCreatedAtDesc(@Param("userId") Integer userId, @Param("deleted") Boolean deleted);
    
    List<Notification> findByUserIdAndReadAndDeletedOrderByCreatedAtDesc(@Param("userId") Integer userId, @Param("read") Boolean read, @Param("deleted") Boolean deleted);
    
    long countByUserIdAndReadAndDeleted(@Param("userId") Integer userId, @Param("read") Boolean read, @Param("deleted") Boolean deleted);
    
    int markAllAsReadByUserId(Integer userId);
    
    int softDeleteById(Integer notificationId);
    
    int softDeleteByIds(@Param("notificationIds") List<Integer> notificationIds);
    
    void insert(Notification notification);
    
    void update(Notification notification);
}
