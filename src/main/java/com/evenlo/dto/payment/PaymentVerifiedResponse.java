package com.evenlo.dto.payment;

import java.util.UUID;

public record PaymentVerifiedResponse(
		UUID bookingId,
		String status
) {
}
