package com.competition.mapper;

import com.competition.entity.Notification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {
    
    @Select("SELECT * FROM Notification WHERE is_deleted = FALSE")
    @Results(id = "notificationResultMap", value = {
        @Result(property = "notificationId", column = "notification_id", id = true),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "notificationType", column = "notification_type"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "read", column = "is_read"),
        @Result(property = "relatedId", column = "related_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deleted", column = "is_deleted")
    })
    List<Notification> findAll();
    
    @Select("SELECT * FROM Notification WHERE notification_id = #{notificationId} AND is_deleted = FALSE")
    @ResultMap("notificationResultMap")
    Notification findById(Integer notificationId);
    
    @Select("SELECT * FROM Notification WHERE user_id = #{userId} AND is_deleted = #{deleted} ORDER BY created_at DESC")
    @ResultMap("notificationResultMap")
    List<Notification> findByUserIdAndDeletedOrderByCreatedAtDesc(@Param("userId") Integer userId, @Param("deleted") Boolean deleted);
    
    @Select("SELECT * FROM Notification WHERE user_id = #{userId} AND is_read = #{read} AND is_deleted = #{deleted} ORDER BY created_at DESC")
    @ResultMap("notificationResultMap")
    List<Notification> findByUserIdAndReadAndDeletedOrderByCreatedAtDesc(@Param("userId") Integer userId, @Param("read") Boolean read, @Param("deleted") Boolean deleted);
    
    @Select("SELECT COUNT(*) FROM Notification WHERE user_id = #{userId} AND is_read = #{read} AND is_deleted = #{deleted}")
    long countByUserIdAndReadAndDeleted(@Param("userId") Integer userId, @Param("read") Boolean read, @Param("deleted") Boolean deleted);
    
    @Update("UPDATE Notification SET is_read = TRUE WHERE user_id = #{userId} AND is_read = FALSE AND is_deleted = FALSE")
    int markAllAsReadByUserId(Integer userId);
    
    @Update("UPDATE Notification SET is_deleted = TRUE WHERE notification_id = #{notificationId}")
    int softDeleteById(Integer notificationId);
    
    @Update("<script>" +
            "UPDATE Notification SET is_deleted = TRUE WHERE notification_id IN " +
            "<foreach item='id' collection='notificationIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int softDeleteByIds(@Param("notificationIds") List<Integer> notificationIds);
    
    @Insert("INSERT INTO Notification (user_id, notification_type, title, content, is_read, related_id, " +
            "created_at, updated_at, is_deleted) " +
            "VALUES (#{userId}, #{notificationType}, #{title}, #{content}, #{read}, #{relatedId}, NOW(), NOW(), FALSE)")
    @Options(useGeneratedKeys = true, keyProperty = "notificationId", keyColumn = "notification_id")
    void insert(Notification notification);
    
    @Update("UPDATE Notification SET user_id = #{userId}, notification_type = #{notificationType}, " +
            "title = #{title}, content = #{content}, is_read = #{read}, related_id = #{relatedId}, updated_at = NOW() " +
            "WHERE notification_id = #{notificationId}")
    void update(Notification notification);
}
