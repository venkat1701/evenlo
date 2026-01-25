package com.evenlo.repository;

import com.evenlo.model.SeatInventory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, UUID> {
	Optional<SeatInventory> findByEventIdAndCategory(UUID eventId, String category);
	List<SeatInventory> findAllByEventId(UUID eventId);
}
