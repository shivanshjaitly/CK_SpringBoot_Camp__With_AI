package in.codekerdos.booking.repository;

/** Interface-based projection for {@link BookingRepository#findProviderBookingStats()}. */
public interface ProviderBookingStats {
    String getProviderName();
    Long getTotalBookings();
    Long getTotalSlots();
}
