package com.evenlo.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record EventUpdateRequest(
		@NotBlank String title,
		@NotBlank String description,
		@NotBlank String city,
		@NotBlank String venueName,
		@NotBlank String venueAddress,
		@NotNull Instant startsAt,
		@NotNull Instant endsAt
) {
}
