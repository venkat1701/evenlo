package com.evenlo.dto.event;

import com.evenlo.model.EventStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
		UUID id,
		String title,
		String description,
		String city,
		String venueName,
		String venueAddress,
		Instant startsAt,
		Instant endsAt,
		EventStatus status,
		UUID createdBy,
		UUID imageFileId,
		List<SeatCategoryResponse> seatCategories
) {
}
