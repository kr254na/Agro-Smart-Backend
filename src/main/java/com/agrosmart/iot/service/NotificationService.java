package com.agrosmart.iot.service;

import com.agrosmart.common.exception.NotAllowedException;
import com.agrosmart.identity.exception.UserNotFoundException;
import com.agrosmart.identity.model.User;
import com.agrosmart.identity.repository.UserRepo;
import com.agrosmart.iot.exception.NotificationNotFoundException;
import com.agrosmart.iot.model.Notification;
import com.agrosmart.iot.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;

    public List<Notification> getNotificationsForUser(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return notificationRepo.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markAsRead(Long id, String email) {
        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        if (!notification.getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized action");
        }

        notification.setRead(true);
        notificationRepo.save(notification);
    }
}