package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.NotificationResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;

public interface NotificationService {
    PagedResponse<NotificationResponse> getUserNotifications(String username, int page, int size);
    long getUnreadCount(String username);
    NotificationResponse markAsRead(String username, Long notificationId);
    void markAllAsRead(String username);
    void sendNotification(Long userId, String title, String message,
                          com.smartretail.pricemonitor.constants.NotificationType type);
}
