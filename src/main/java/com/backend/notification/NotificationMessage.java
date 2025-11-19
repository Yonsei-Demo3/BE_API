package com.backend.notification;

public record NotificationMessage(
        Long receiverId,
        NotificationType type
) {
    public static NotificationMessage from(Notification notification) {
        return new NotificationMessage(notification.getReceiver().getId(),  notification.getType());
    }

    public static NotificationMessage of(Long receiverId, NotificationType type) {
        return new NotificationMessage(receiverId, type);
    }
}
