package in.codekerdos.booking.web;

import java.time.Instant;

/** Local shape for notification-service's /api/notifications response — just enough to render it. */
public record NotificationView(
        Long id,
        Long bookingId,
        String customerEmail,
        String eventType,
        String message,
        Instant sentAt
) {
}
