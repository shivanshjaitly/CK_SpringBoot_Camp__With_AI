package in.codekerdos.booking.dto;

import in.codekerdos.booking.enums.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateSlotRequest(
        @NotBlank String title,
        @Size(max = 2000) String description,
        @NotNull ResourceType resourceType,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Size(max = 255) String location,
        @Min(1) int capacity
) {}
