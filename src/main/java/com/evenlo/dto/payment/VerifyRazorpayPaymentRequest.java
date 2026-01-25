package com.evenlo.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VerifyRazorpayPaymentRequest(
		@NotNull UUID bookingId,
		@NotBlank String razorpayOrderId,
		@NotBlank String razorpayPaymentId,
		@NotBlank String razorpaySignature
) {
}
