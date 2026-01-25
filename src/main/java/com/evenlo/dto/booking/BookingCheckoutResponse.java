package com.evenlo.dto.booking;

import java.util.UUID;

public record BookingCheckoutResponse(
		BookingResponse booking,
		String razorpayKeyId,
		String razorpayOrderId
) {
	public UUID bookingId() {
		return booking.id();
	}
}
