package in.codekerdos.booking.event;

import in.codekerdos.booking.entity.OutboxEvent;
import in.codekerdos.booking.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Writes the "an event must be sent" fact into the outbox table.
 * Callers invoke this from inside an already-open @Transactional method
 * so the outbox row commits atomically with the Booking/Slot state change.
 * No Kafka dependency here on purpose — this class never talks to the broker.
 */
@Component
public class OutboxEventRecorder {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventRecorder(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void recordConfirmed(Long bookingId, Long slotId, String customerEmail) {
        record(BookingEvent.TYPE_CONFIRMED, bookingId, slotId, customerEmail);
    }

    public void recordCancelled(Long bookingId, Long slotId, String customerEmail) {
        record(BookingEvent.TYPE_CANCELLED, bookingId, slotId, customerEmail);
    }

    private void record(String eventType, Long bookingId, Long slotId, String customerEmail) {
        OutboxEvent event = new OutboxEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setBookingId(bookingId);
        event.setSlotId(slotId);
        event.setCustomerEmail(customerEmail);
        outboxEventRepository.save(event);
    }
}
