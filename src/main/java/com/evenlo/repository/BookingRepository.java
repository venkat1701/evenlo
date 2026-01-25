package com.evenlo.repository;

import com.evenlo.model.Booking;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
	Page<Booking> findByUserId(UUID userId, Pageable pageable);
	Page<Booking> findByEventId(UUID eventId, Pageable pageable);
}
