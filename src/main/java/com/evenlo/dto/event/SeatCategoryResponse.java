package com.evenlo.dto.event;

public record SeatCategoryResponse(
		String category,
		long pricePerSeatPaise,
		int totalCount,
		int availableCount
) {
}
