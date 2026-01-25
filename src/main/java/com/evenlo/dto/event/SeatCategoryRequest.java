package com.evenlo.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SeatCategoryRequest(
		@NotBlank String category,
		@Positive long pricePerSeatPaise,
		@Positive int totalCount
) {
}
