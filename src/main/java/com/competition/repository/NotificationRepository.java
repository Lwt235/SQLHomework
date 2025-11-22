package com.competition.repository;

import com.competition.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Integer userId, Boolean read);
    long countByUserIdAndRead(Integer userId, Boolean read);
}
