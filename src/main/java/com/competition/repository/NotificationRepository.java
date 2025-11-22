package com.competition.repository;

import com.competition.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserIdAndDeletedOrderByCreatedAtDesc(Integer userId, Boolean deleted);
    List<Notification> findByUserIdAndReadAndDeletedOrderByCreatedAtDesc(Integer userId, Boolean read, Boolean deleted);
    long countByUserIdAndReadAndDeleted(Integer userId, Boolean read, Boolean deleted);
    
    /**
     * Bulk update to mark all unread notifications as read for a user.
     * Note: This method must be called within a transactional context (e.g., from NotificationService).
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.read = false AND n.deleted = false")
    int markAllAsReadByUserId(@Param("userId") Integer userId);
    
    /**
     * Soft delete a notification by setting deleted flag to true
     */
    @Modifying
    @Query("UPDATE Notification n SET n.deleted = true WHERE n.notificationId = :notificationId")
    int softDeleteById(@Param("notificationId") Integer notificationId);
    
    /**
     * Batch soft delete notifications by setting deleted flag to true
     */
    @Modifying
    @Query("UPDATE Notification n SET n.deleted = true WHERE n.notificationId IN :notificationIds")
    int softDeleteByIds(@Param("notificationIds") List<Integer> notificationIds);
}