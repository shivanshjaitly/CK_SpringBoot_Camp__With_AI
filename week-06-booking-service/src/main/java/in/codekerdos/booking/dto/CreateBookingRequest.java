package in.codekerdos.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
        @NotNull Long slotId,
        @Size(max = 500) String notes
) {}
