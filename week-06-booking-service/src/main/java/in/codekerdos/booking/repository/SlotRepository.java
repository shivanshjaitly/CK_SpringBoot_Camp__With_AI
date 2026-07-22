package in.codekerdos.booking.repository;

import in.codekerdos.booking.entity.Slot;
import in.codekerdos.booking.enums.ResourceType;
import in.codekerdos.booking.enums.SlotStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    @EntityGraph(attributePaths = {"provider"})
    List<Slot> findByStatus(SlotStatus status);

    @EntityGraph(attributePaths = {"provider"})
    List<Slot> findByResourceTypeAndStatus(ResourceType type, SlotStatus status);

    @EntityGraph(attributePaths = {"provider"})
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findByIdWithProvider(@Param("id") Long id);
}
