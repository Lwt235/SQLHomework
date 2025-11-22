package com.competition.service;

import com.competition.entity.Notification;
import com.competition.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Create a new notification
     */
    @Transactional
    public Notification createNotification(Integer userId, String type, String title, String content, Integer relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRead(false);
        notification.setDeleted(false);
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Get all non-deleted notifications for a user
     */
    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdAndDeletedOrderByCreatedAtDesc(userId, false);
    }
    
    /**
     * Get unread non-deleted notifications for a user
     */
    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationRepository.findByUserIdAndReadAndDeletedOrderByCreatedAtDesc(userId, false, false);
    }
    
    /**
     * Get unread notification count (excluding deleted)
     */
    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndReadAndDeleted(userId, false, false);
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public Notification markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Integer userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
    
    /**
     * Soft delete a notification
     */
    @Transactional
    public void deleteNotification(Integer notificationId) {
        int updated = notificationRepository.softDeleteById(notificationId);
        if (updated == 0) {
            throw new RuntimeException("通知不存在或已删除");
        }
    }
    
    /**
     * Batch soft delete notifications
     */
    @Transactional
    public void batchDeleteNotifications(List<Integer> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            throw new IllegalArgumentException("通知ID列表不能为空");
        }
        int updated = notificationRepository.softDeleteByIds(notificationIds);
        if (updated < notificationIds.size()) {
            // Some notifications might already be deleted or not exist
            // Log warning but don't fail the operation
        }
    }
}
