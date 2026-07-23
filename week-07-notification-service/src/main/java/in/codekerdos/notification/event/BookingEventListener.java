package in.codekerdos.notification.event;

import in.codekerdos.notification.entity.Notification;
import in.codekerdos.notification.entity.ProcessedEvent;
import in.codekerdos.notification.repository.NotificationRepository;
import in.codekerdos.notification.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reacts to booking.events. This is the "notification" side of the
 * choreography saga: CONFIRMED sends a confirmation, CANCELLED sends the
 * compensating cancellation notice — no orchestrator tells it what to do.
 */
@Component
public class BookingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    private final ProcessedEventRepository processedEventRepository;
    private final NotificationRepository notificationRepository;

    public BookingEventListener(ProcessedEventRepository processedEventRepository,
                                 NotificationRepository notificationRepository) {
        this.processedEventRepository = processedEventRepository;
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "booking.events",
            groupId = "notification-service",
            autoStartup = "${app.kafka-listener.enabled:true}"
    )
    @Transactional
    public void onBookingEvent(BookingEvent event) {
        if (processedEventRepository.existsByEventId(event.eventId())) {
            log.info("Duplicate delivery of {} (eventId={}) — already processed, skipping", event.type(), event.eventId());
            return;
        }

        String message = switch (event.type()) {
            case BookingEvent.TYPE_CONFIRMED ->
                    "Your booking #" + event.bookingId() + " is confirmed. See you there!";
            case BookingEvent.TYPE_CANCELLED ->
                    "Your booking #" + event.bookingId() + " has been cancelled. The slot is now free for others.";
            default -> "Update on booking #" + event.bookingId() + ": " + event.type();
        };

        Notification notification = new Notification();
        notification.setBookingId(event.bookingId());
        notification.setCustomerEmail(event.customerEmail());
        notification.setEventType(event.type());
        notification.setMessage(message);
        notificationRepository.save(notification);

        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventId(event.eventId());
        processedEventRepository.save(processed);

        log.info("Notified {} — {}", event.customerEmail(), message);
    }
}
