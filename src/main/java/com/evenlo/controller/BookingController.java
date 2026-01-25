package com.evenlo.controller;

import com.evenlo.dto.booking.BookingCheckoutResponse;
import com.evenlo.dto.booking.BookingCreateRequest;
import com.evenlo.dto.booking.BookingResponse;
import com.evenlo.service.BookingService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping
	public BookingCheckoutResponse create(@Valid @RequestBody BookingCreateRequest request) {
		return bookingService.createCheckout(request);
	}

	@GetMapping("/{id}")
	public BookingResponse get(@PathVariable UUID id) {
		return bookingService.get(id);
	}

	@GetMapping
	public Page<BookingResponse> myBookings(Pageable pageable) {
		return bookingService.myBookings(pageable);
	}

	@PutMapping("/{id}/cancel")
	public BookingResponse cancel(@PathVariable UUID id) {
		return bookingService.cancel(id);
	}
}
