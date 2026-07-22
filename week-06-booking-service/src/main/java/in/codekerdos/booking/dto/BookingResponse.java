package in.codekerdos.booking.dto;

import in.codekerdos.booking.entity.Booking;
import in.codekerdos.booking.enums.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long slotId,
        String slotTitle,
        String customerEmail,
        BookingStatus status,
        String notes,
        String idempotencyKey,
        LocalDateTime createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getSlot().getId(),
                booking.getSlot().getTitle(),
                booking.getCustomer().getEmail(),
                booking.getStatus(),
                booking.getNotes(),
                booking.getIdempotencyKey(),
                booking.getCreatedAt()
        );
    }
}
