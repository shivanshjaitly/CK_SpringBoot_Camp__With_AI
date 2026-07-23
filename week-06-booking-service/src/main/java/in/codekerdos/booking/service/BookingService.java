package in.codekerdos.booking.service;

import in.codekerdos.booking.dto.BookingResponse;
import in.codekerdos.booking.dto.CreateBookingRequest;
import in.codekerdos.booking.dto.ProviderStatsResponse;
import in.codekerdos.booking.entity.AppUser;
import in.codekerdos.booking.entity.Booking;
import in.codekerdos.booking.entity.Slot;
import in.codekerdos.booking.enums.BookingStatus;
import in.codekerdos.booking.enums.SlotStatus;
import in.codekerdos.booking.event.OutboxEventRecorder;
import in.codekerdos.booking.exception.BusinessException;
import in.codekerdos.booking.exception.ResourceNotFoundException;
import in.codekerdos.booking.repository.AppUserRepository;
import in.codekerdos.booking.repository.BookingRepository;
import in.codekerdos.booking.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final AppUserRepository userRepository;
    private final OutboxEventRecorder outboxEventRecorder;

    public BookingService(
            BookingRepository bookingRepository,
            SlotRepository slotRepository,
            AppUserRepository userRepository,
            OutboxEventRecorder outboxEventRecorder
    ) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.outboxEventRecorder = outboxEventRecorder;
    }

    /**
     * Class 1: create booking. Class 2: pass Idempotency-Key — same key returns existing booking.
     */
    @Transactional
    public BookingResponse create(CreateBookingRequest request, String customerEmail, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return bookingRepository.findByIdempotencyKey(idempotencyKey)
                    .map(BookingResponse::from)
                    .orElseGet(() -> createNew(request, customerEmail, idempotencyKey));
        }
        return createNew(request, customerEmail, null);
    }

    private BookingResponse createNew(CreateBookingRequest request, String customerEmail, String idempotencyKey) {
        AppUser customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Slot slot = slotRepository.findByIdWithProvider(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.OPEN) {
            throw new BusinessException("Slot is not open for booking");
        }
        if (slot.getBookedCount() >= slot.getCapacity()) {
            throw new BusinessException("Slot is full");
        }

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.PENDING);
        BookingStateMachine.assertTransition(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setNotes(request.notes());
        booking.setIdempotencyKey(idempotencyKey);
        bookingRepository.save(booking);

        slot.setBookedCount(slot.getBookedCount() + 1);
        if (slot.getBookedCount() >= slot.getCapacity()) {
            slot.setStatus(SlotStatus.FULL);
        }

        // Same transaction as the Booking/Slot write — the outbox row and the state
        // change commit together or not at all (transactional outbox pattern).
        outboxEventRecorder.recordConfirmed(booking.getId(), slot.getId(), customer.getEmail());

        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findMine(String customerEmail) {
        return bookingRepository.findByCustomer_Email(customerEmail).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findAll() {
        return bookingRepository.findAllWithDetails().stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, String customerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new BusinessException("You can only cancel your own bookings");
        }

        BookingStateMachine.assertTransition(booking.getStatus(), BookingStatus.CANCELLED);
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        Slot slot = booking.getSlot();
        if (slot.getBookedCount() > 0) {
            slot.setBookedCount(slot.getBookedCount() - 1);
        }
        if (slot.getStatus() == SlotStatus.FULL) {
            slot.setStatus(SlotStatus.OPEN);
        }

        // Compensating event for the choreography saga — notification-service
        // reacts by sending a cancellation notice (no orchestrator class).
        outboxEventRecorder.recordCancelled(booking.getId(), slot.getId(), booking.getCustomer().getEmail());

        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<ProviderStatsResponse> providerBookingStats() {
        return bookingRepository.findProviderBookingStats().stream()
                .map(ProviderStatsResponse::from)
                .toList();
    }
}
