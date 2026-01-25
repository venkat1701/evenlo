package com.evenlo.dto.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record BookingCreateRequest(
		@NotNull UUID eventId,
		@NotBlank String seatCategory,
		@Positive int quantity
) {
}
