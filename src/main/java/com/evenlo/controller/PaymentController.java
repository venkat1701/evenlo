package com.evenlo.controller;

import com.evenlo.dto.payment.PaymentVerifiedResponse;
import com.evenlo.dto.payment.VerifyRazorpayPaymentRequest;
import com.evenlo.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/razorpay/verify")
	public PaymentVerifiedResponse verify(@Valid @RequestBody VerifyRazorpayPaymentRequest request) {
		paymentService.verifyRazorpayPayment(
				request.bookingId(),
				request.razorpayOrderId(),
				request.razorpayPaymentId(),
				request.razorpaySignature()
		);
		return new PaymentVerifiedResponse(request.bookingId(), "VERIFIED");
	}
}
