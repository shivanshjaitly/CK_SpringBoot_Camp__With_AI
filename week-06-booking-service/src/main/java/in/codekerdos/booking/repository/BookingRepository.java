package in.codekerdos.booking.repository;

import in.codekerdos.booking.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"slot", "customer"})
    @Query("select b from Booking b where b.customer.email = :email")
    List<Booking> findByCustomer_Email(@Param("email") String email);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = {"slot", "customer"})
    @Query("select b from Booking b")
    List<Booking> findAllWithDetails();

    /**
     * Native SQL on purpose: a 3-table aggregate (providers → their slots → bookings on
     * those slots) with a conditional COUNT is awkward in JPQL — no CASE-in-aggregate
     * without extra hoops, and no clean way to express COUNT(DISTINCT s.id) alongside a
     * filtered booking count. Plain SQL is the more direct tool for this reporting query.
     */
    @Query(value = """
            SELECT
                u.full_name AS providerName,
                COUNT(CASE WHEN b.status = 'CONFIRMED' THEN 1 END) AS totalBookings,
                COUNT(DISTINCT s.id) AS totalSlots
            FROM app_users u
            JOIN slots s ON s.provider_id = u.id
            LEFT JOIN bookings b ON b.slot_id = s.id
            GROUP BY u.full_name
            ORDER BY totalBookings DESC
            """, nativeQuery = true)
    List<ProviderBookingStats> findProviderBookingStats();
}
