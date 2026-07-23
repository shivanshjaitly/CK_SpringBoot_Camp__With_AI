package in.codekerdos.notification.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Idempotent-consumer ledger. Kafka delivery is at-least-once — the same
 * eventId can arrive twice (producer retry, consumer rebalance). Before
 * acting on an event we check this table; a hit means "already handled,
 * skip silently."
 */
@Entity
@Table(name = "processed_events", indexes = {
        @Index(name = "idx_processed_event_id", columnList = "eventId", unique = true)
})
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(nullable = false, updatable = false)
    private Instant processedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
