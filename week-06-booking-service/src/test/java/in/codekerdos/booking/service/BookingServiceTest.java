package in.codekerdos.booking.service;

import in.codekerdos.booking.dto.CreateBookingRequest;
import in.codekerdos.booking.entity.AppUser;
import in.codekerdos.booking.entity.Booking;
import in.codekerdos.booking.entity.Slot;
import in.codekerdos.booking.enums.BookingStatus;
import in.codekerdos.booking.enums.Role;
import in.codekerdos.booking.enums.SlotStatus;
import in.codekerdos.booking.exception.BusinessException;
import in.codekerdos.booking.repository.AppUserRepository;
import in.codekerdos.booking.repository.BookingRepository;
import in.codekerdos.booking.repository.SlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    SlotRepository slotRepository;
    @Mock
    AppUserRepository userRepository;

    @InjectMocks
    BookingService bookingService;

    @Test
    void create_whenSlotFull_throws() {
        AppUser customer = new AppUser();
        customer.setEmail("customer@codekerdos.in");
        customer.setRole(Role.CUSTOMER);

        Slot slot = new Slot();
        slot.setId(1L);
        slot.setStatus(SlotStatus.OPEN);
        slot.setCapacity(1);
        slot.setBookedCount(1);
        slot.setTitle("Full slot");
        slot.setProvider(new AppUser());

        when(userRepository.findByEmail("customer@codekerdos.in")).thenReturn(Optional.of(customer));
        when(slotRepository.findByIdWithProvider(1L)).thenReturn(Optional.of(slot));

        assertThrows(BusinessException.class,
                () -> bookingService.create(new CreateBookingRequest(1L, "x"), "customer@codekerdos.in", null));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenIdempotencyExists_returnsExisting() {
        AppUser customer = new AppUser();
        customer.setEmail("customer@codekerdos.in");

        Slot slot = new Slot();
        slot.setId(1L);
        slot.setTitle("Room");
        slot.setProvider(new AppUser());

        Booking existing = new Booking();
        existing.setId(99L);
        existing.setSlot(slot);
        existing.setCustomer(customer);
        existing.setStatus(BookingStatus.CONFIRMED);
        existing.setIdempotencyKey("key-1");

        when(bookingRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(existing));

        var response = bookingService.create(new CreateBookingRequest(1L, "n"), "customer@codekerdos.in", "key-1");

        assertEquals(99L, response.id());
        verify(bookingRepository, never()).save(any());
    }
}
