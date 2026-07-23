package in.codekerdos.notification.dto;

import in.codekerdos.notification.entity.Notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long bookingId,
        String customerEmail,
        String eventType,
        String message,
        Instant sentAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getBookingId(),
                notification.getCustomerEmail(),
                notification.getEventType(),
                notification.getMessage(),
                notification.getSentAt()
        );
    }
}
