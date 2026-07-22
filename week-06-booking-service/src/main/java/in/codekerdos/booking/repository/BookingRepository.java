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
}
