package com.evenlo.dto.booking;

import com.evenlo.model.BookingStatus;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
		UUID id,
		UUID eventId,
		UUID userId,
		String seatCategory,
		int quantity,
		long totalAmountPaise,
		String currency,
		BookingStatus status,
		Instant createdAt
) {
}
