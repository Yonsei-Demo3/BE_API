package com.backend.notification;

import java.util.Map;

public record NotificationMessage(
        Long receiverId,
        NotificationType type,
        String message,
        Map<String, Object> data
) {
    /*
    public static NotificationMessage from(Notification notification,) {
        return new NotificationMessage(notification.getReceiver().getId(),  notification.getType());
    }

     */

    public static NotificationMessage of(Long receiverId, NotificationType type, String message, Map<String, Object> data) {
        return new NotificationMessage(receiverId, type, message, data);
    }
}
