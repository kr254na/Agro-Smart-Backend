package com.agrosmart.iot.repository;

import com.agrosmart.identity.model.User;
import com.agrosmart.iot.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    // Get all notifications for a specific user, newest first
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Useful for showing an 'unread' count on the frontend
    long countByUserAndIsReadFalse(User user);
}