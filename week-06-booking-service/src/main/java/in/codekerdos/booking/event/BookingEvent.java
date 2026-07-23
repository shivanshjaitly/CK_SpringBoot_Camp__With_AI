package in.codekerdos.booking.event;

import java.time.Instant;

/**
 * Wire contract published to the {@code booking.events} Kafka topic.
 * Consumers (notification-service) own their own copy of this shape —
 * services are decoupled at the JSON contract, not via a shared JAR.
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
