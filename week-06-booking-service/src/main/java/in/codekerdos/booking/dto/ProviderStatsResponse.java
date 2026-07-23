package in.codekerdos.booking.dto;

import in.codekerdos.booking.repository.ProviderBookingStats;

public record ProviderStatsResponse(
        String providerName,
        Long totalBookings,
        Long totalSlots
) {
    public static ProviderStatsResponse from(ProviderBookingStats stats) {
        return new ProviderStatsResponse(stats.getProviderName(), stats.getTotalBookings(), stats.getTotalSlots());
    }
}
