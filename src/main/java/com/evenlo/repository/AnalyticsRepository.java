package com.evenlo.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AnalyticsRepository extends Repository<Object, UUID> {
	@Query(
			value = "SELECT e.id AS eventId, e.title AS title, COUNT(b.id) AS bookingsCount, COALESCE(SUM(CASE WHEN b.status = 'CONFIRMED' THEN b.total_amount_paise ELSE 0 END), 0) AS revenuePaise " +
					"FROM events e " +
					"LEFT JOIN bookings b ON b.event_id = e.id AND b.created_at >= :from AND b.created_at <= :to " +
					"GROUP BY e.id, e.title " +
					"ORDER BY revenuePaise DESC " +
					"LIMIT :limit",
			nativeQuery = true
	)
	List<TopEventRow> topEvents(@Param("from") Instant from, @Param("to") Instant to, @Param("limit") int limit);

	@Query(
			value = "SELECT b.event_id AS eventId, COUNT(*) AS totalBookings, " +
					"COALESCE(SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END), 0) AS confirmedBookings, " +
					"COALESCE(SUM(CASE WHEN b.status = 'CONFIRMED' THEN b.total_amount_paise ELSE 0 END), 0) AS revenuePaise, " +
					"COALESCE(SUM(CASE WHEN b.status = 'CONFIRMED' THEN b.quantity ELSE 0 END), 0) AS seatsSold " +
					"FROM bookings b WHERE b.event_id = :eventId GROUP BY b.event_id",
			nativeQuery = true
	)
	EventSummaryRow eventSummary(@Param("eventId") UUID eventId);

	interface TopEventRow {
		UUID getEventId();

		String getTitle();

		long getBookingsCount();

		long getRevenuePaise();
	}

	interface EventSummaryRow {
		UUID getEventId();

		long getTotalBookings();

		long getConfirmedBookings();

		long getRevenuePaise();

		long getSeatsSold();
	}
}
