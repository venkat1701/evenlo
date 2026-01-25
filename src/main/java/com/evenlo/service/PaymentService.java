package com.evenlo.service;

import com.evenlo.config.AppProperties;
import com.evenlo.exception.BadRequestException;
import com.evenlo.exception.NotFoundException;
import com.evenlo.model.Booking;
import com.evenlo.model.BookingStatus;
import com.evenlo.model.Payment;
import com.evenlo.model.PaymentProvider;
import com.evenlo.model.PaymentStatus;
import com.evenlo.repository.BookingRepository;
import com.evenlo.repository.PaymentRepository;
import com.evenlo.dto.kafka.BookingConfirmedEvent;
import com.evenlo.util.HmacSha256;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class PaymentService {
	private final AppProperties props;
	private final BookingRepository bookingRepository;
	private final PaymentRepository paymentRepository;
	private final KafkaEventPublisher eventPublisher;
	private final RestClient restClient;

	public PaymentService(AppProperties props, BookingRepository bookingRepository, PaymentRepository paymentRepository, KafkaEventPublisher eventPublisher) {
		this.props = props;
		this.bookingRepository = bookingRepository;
		this.paymentRepository = paymentRepository;
		this.eventPublisher = eventPublisher;
		this.restClient = RestClient.builder().baseUrl(props.getIntegration().getRazorpay().getBaseUrl()).build();
	}

	@Transactional
	public Payment createRazorpayOrder(UUID bookingId) {
		Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("booking_not_found"));
		if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
			throw new BadRequestException("booking_not_payable");
		}

		paymentRepository.findByBookingId(bookingId).ifPresent(p -> {
			throw new BadRequestException("payment_already_created");
		});

		Map<String, Object> payload = Map.of(
				"amount", booking.getTotalAmountPaise(),
				"currency", booking.getCurrency(),
				"receipt", booking.getId().toString(),
				"notes", Map.of("bookingId", booking.getId().toString())
		);

		RazorpayOrderResponse response = restClient.post()
				.uri("/v1/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", basicAuthHeader())
				.body(payload)
				.retrieve()
				.body(RazorpayOrderResponse.class);

		if (response == null || response.id() == null || response.id().isBlank()) {
			throw new IllegalStateException("razorpay_order_failed");
		}

		Payment payment = new Payment();
		payment.setId(UUID.randomUUID());
		payment.setBookingId(booking.getId());
		payment.setProvider(PaymentProvider.RAZORPAY);
		payment.setProviderOrderId(response.id());
		payment.setProviderPaymentId(null);
		payment.setAmountPaise(booking.getTotalAmountPaise());
		payment.setCurrency(booking.getCurrency());
		payment.setStatus(PaymentStatus.ORDER_CREATED);

		return paymentRepository.save(payment);
	}

	@Transactional
	public void verifyRazorpayPayment(UUID bookingId, String orderId, String paymentId, String signature) {
		Payment payment = paymentRepository.findByBookingId(bookingId).orElseThrow(() -> new NotFoundException("payment_not_found"));
		if (!payment.getProviderOrderId().equals(orderId)) {
			throw new BadRequestException("order_mismatch");
		}
		if (payment.getStatus() == PaymentStatus.VERIFIED) {
			return;
		}

		String payload = orderId + "|" + paymentId;
		String expectedSignature = HmacSha256.hex(props.getIntegration().getRazorpay().getKeySecret(), payload);
		if (!constantTimeEquals(expectedSignature, signature)) {
			payment.setStatus(PaymentStatus.FAILED);
			paymentRepository.save(payment);
			throw new BadRequestException("invalid_signature");
		}

		payment.setProviderPaymentId(paymentId);
		payment.setStatus(PaymentStatus.VERIFIED);
		paymentRepository.save(payment);

		Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("booking_not_found"));
		booking.setStatus(BookingStatus.CONFIRMED);
		Booking savedBooking = bookingRepository.save(booking);

		eventPublisher.publishBookingConfirmed(new BookingConfirmedEvent(
				savedBooking.getId(),
				savedBooking.getEventId(),
				savedBooking.getUserId(),
				savedBooking.getTotalAmountPaise(),
				savedBooking.getCurrency(),
				Instant.now()
		));
	}

	public String razorpayKeyId() {
		return props.getIntegration().getRazorpay().getKeyId();
	}

	private String basicAuthHeader() {
		String raw = props.getIntegration().getRazorpay().getKeyId() + ":" + props.getIntegration().getRazorpay().getKeySecret();
		String b64 = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
		return "Basic " + b64;
	}

	private boolean constantTimeEquals(String a, String b) {
		if (a == null || b == null) {
			return false;
		}
		if (a.length() != b.length()) {
			return false;
		}
		int r = 0;
		for (int i = 0; i < a.length(); i++) {
			r |= a.charAt(i) ^ b.charAt(i);
		}
		return r == 0;
	}

	public record RazorpayOrderResponse(
			String id,
			String entity,
			long amount,
			String currency,
			String receipt,
			String status,
			long created_at
	) {
	}
}
