package in.codekerdos.booking.event;

import in.codekerdos.booking.entity.OutboxEvent;
import in.codekerdos.booking.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Polls the outbox for unpublished rows and relays them to Kafka.
 * Delivery is at-least-once: if the broker ack times out after the row was
 * actually written, the same row is retried on the next tick — consumers
 * (notification-service) MUST dedupe by eventId.
 * Disabled in tests via app.kafka.enabled=false so `mvn test` doesn't need a
 * live broker.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", matchIfMissing = true)
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final String TOPIC = "booking.events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public OutboxRelay(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    public void relayPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByPublishedFalseOrderByOccurredAtAsc();
        for (OutboxEvent row : pending) {
            publish(row);
        }
    }

    private void publish(OutboxEvent row) {
        BookingEvent event = new BookingEvent(
                row.getEventId(),
                row.getEventType(),
                row.getOccurredAt(),
                row.getBookingId(),
                row.getSlotId(),
                row.getCustomerEmail()
        );
        try {
            // Key by bookingId so CONFIRMED/CANCELLED for the same booking stay ordered on one partition.
            kafkaTemplate.send(TOPIC, String.valueOf(row.getBookingId()), event).get(5, TimeUnit.SECONDS);
            row.setPublished(true);
            row.setPublishedAt(Instant.now());
            outboxEventRepository.save(row);
            log.info("Relayed {} for booking {} (eventId={})", row.getEventType(), row.getBookingId(), row.getEventId());
        } catch (Exception ex) {
            log.warn("Relay failed for outbox row {} ({}) — will retry next tick: {}",
                    row.getId(), row.getEventType(), ex.getMessage());
        }
    }
}
