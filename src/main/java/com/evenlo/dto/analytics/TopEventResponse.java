package com.evenlo.dto.analytics;

import java.util.UUID;

public record TopEventResponse(
		UUID eventId,
		String title,
		long bookingsCount,
		long revenuePaise
) {
}
