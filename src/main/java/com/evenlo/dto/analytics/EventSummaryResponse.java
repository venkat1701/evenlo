package com.evenlo.dto.analytics;

import java.util.UUID;

public record EventSummaryResponse(
		UUID eventId,
		long totalBookings,
		long confirmedBookings,
		long seatsSold,
		long revenuePaise
) {
}
