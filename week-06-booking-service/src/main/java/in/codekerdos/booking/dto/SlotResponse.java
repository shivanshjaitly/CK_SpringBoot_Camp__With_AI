package in.codekerdos.booking.dto;

import in.codekerdos.booking.entity.Slot;
import in.codekerdos.booking.enums.ResourceType;
import in.codekerdos.booking.enums.SlotStatus;

import java.time.LocalDateTime;

public record SlotResponse(
        Long id,
        String title,
        String description,
        ResourceType resourceType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        int capacity,
        int bookedCount,
        SlotStatus status,
        String providerEmail
) {
    public static SlotResponse from(Slot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getTitle(),
                slot.getDescription(),
                slot.getResourceType(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getLocation(),
                slot.getCapacity(),
                slot.getBookedCount(),
                slot.getStatus(),
                slot.getProvider().getEmail()
        );
    }
}
