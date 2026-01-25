package com.evenlo.dto.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record EventCreateRequest(
		@NotBlank String title,
		@NotBlank String description,
		@NotBlank String city,
		@NotBlank String venueName,
		@NotBlank String venueAddress,
		@NotNull Instant startsAt,
		@NotNull Instant endsAt,
		@NotEmpty @Valid List<SeatCategoryRequest> seatCategories
) {
}
