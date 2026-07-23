package in.codekerdos.notification.event;

import java.time.Instant;

/**
 * notification-service's OWN copy of the booking.events contract.
 * Deliberately not shared as a library with booking-service — each service
 * owns its side of the JSON contract, so either side can evolve its Java
 * types independently as long as the JSON shape stays compatible.
 */
public record BookingEvent(
        String eventId,
        String type,
        Instant occurredAt,
        Long bookingId,
        Long slotId,
        String customerEmail
) {
    public static final String TYPE_CONFIRMED = "BookingConfirmed";
    public static final String TYPE_CANCELLED = "BookingCancelled";
}
