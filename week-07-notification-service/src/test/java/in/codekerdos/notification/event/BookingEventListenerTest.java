package in.codekerdos.notification.event;

import in.codekerdos.notification.repository.NotificationRepository;
import in.codekerdos.notification.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingEventListenerTest {

    @Mock
    ProcessedEventRepository processedEventRepository;
    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    BookingEventListener listener;

    @Test
    void onBookingEvent_whenNew_savesNotificationAndMarksProcessed() {
        BookingEvent event = new BookingEvent(
                "evt-1", BookingEvent.TYPE_CONFIRMED, Instant.now(), 42L, 7L, "customer@codekerdos.in");
        when(processedEventRepository.existsByEventId("evt-1")).thenReturn(false);

        listener.onBookingEvent(event);

        verify(notificationRepository).save(any());
        verify(processedEventRepository).save(any());
    }

    @Test
    void onBookingEvent_whenDuplicate_skipsSilently() {
        BookingEvent event = new BookingEvent(
                "evt-1", BookingEvent.TYPE_CONFIRMED, Instant.now(), 42L, 7L, "customer@codekerdos.in");
        when(processedEventRepository.existsByEventId("evt-1")).thenReturn(true);

        listener.onBookingEvent(event);

        verify(notificationRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }
}
