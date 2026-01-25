package com.evenlo.dto.kafka;

import java.time.Instant;
import java.util.UUID;

public record BookingConfirmedEvent(
		UUID bookingId,
		UUID eventId,
		UUID userId,
		long amountPaise,
		String currency,
		Instant confirmedAt
) {
}
