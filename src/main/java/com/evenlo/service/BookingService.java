package com.evenlo.service;

import com.evenlo.dto.booking.BookingCheckoutResponse;
import com.evenlo.dto.booking.BookingCreateRequest;
import com.evenlo.dto.booking.BookingResponse;
import com.evenlo.exception.BadRequestException;
import com.evenlo.exception.NotFoundException;
import com.evenlo.model.Booking;
import com.evenlo.model.BookingStatus;
import com.evenlo.model.Event;
import com.evenlo.model.EventMembership;
import com.evenlo.model.EventMembershipRole;
import com.evenlo.model.EventStatus;
import com.evenlo.model.SeatInventory;
import com.evenlo.repository.BookingRepository;
import com.evenlo.repository.EventMembershipRepository;
import com.evenlo.repository.EventRepository;
import com.evenlo.repository.SeatInventoryRepository;
import com.evenlo.util.SecurityUtil;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
	private final EventRepository eventRepository;
	private final SeatInventoryRepository seatInventoryRepository;
	private final BookingRepository bookingRepository;
	private final EventMembershipRepository membershipRepository;
	private final PaymentService paymentService;
	private final HotAvailabilityService hotAvailabilityService;

	public BookingService(
			EventRepository eventRepository,
			SeatInventoryRepository seatInventoryRepository,
			BookingRepository bookingRepository,
			EventMembershipRepository membershipRepository,
			PaymentService paymentService,
			HotAvailabilityService hotAvailabilityService
	) {
		this.eventRepository = eventRepository;
		this.seatInventoryRepository = seatInventoryRepository;
		this.bookingRepository = bookingRepository;
		this.membershipRepository = membershipRepository;
		this.paymentService = paymentService;
		this.hotAvailabilityService = hotAvailabilityService;
	}

	@Transactional
	public BookingCheckoutResponse createCheckout(BookingCreateRequest request) {
		UUID userId = SecurityUtil.currentUserId();
		Event event = eventRepository.findById(request.eventId()).orElseThrow(() -> new NotFoundException("event_not_found"));
		if (event.getStatus() != EventStatus.PUBLISHED) {
			throw new BadRequestException("event_not_bookable");
		}

		ensureParticipantMembership(event.getId(), userId);

		SeatInventory inventory = reserveSeatsWithOptimisticLock(event.getId(), request.seatCategory().trim(), request.quantity());
		long total = inventory.getPricePerSeatPaise() * (long) request.quantity();

		Booking booking = new Booking();
		booking.setId(UUID.randomUUID());
		booking.setEventId(event.getId());
		booking.setUserId(userId);
		booking.setSeatCategory(inventory.getCategory());
		booking.setQuantity(request.quantity());
		booking.setTotalAmountPaise(total);
		booking.setCurrency("INR");
		booking.setStatus(BookingStatus.PENDING_PAYMENT);

		Booking saved = bookingRepository.save(booking);
		var payment = paymentService.createRazorpayOrder(saved.getId());
		return new BookingCheckoutResponse(toResponse(saved), paymentService.razorpayKeyId(), payment.getProviderOrderId());
	}

	@Transactional(readOnly = true)
	public BookingResponse get(UUID bookingId) {
		Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("booking_not_found"));
		UUID userId = SecurityUtil.currentUserId();
		if (!booking.getUserId().equals(userId) && !SecurityUtil.hasRole("ROLE_PLATFORM_ADMIN")) {
			throw new BadRequestException("not_allowed");
		}
		return toResponse(booking);
	}

	@Transactional(readOnly = true)
	public Page<BookingResponse> myBookings(Pageable pageable) {
		UUID userId = SecurityUtil.currentUserId();
		return bookingRepository.findByUserId(userId, pageable).map(this::toResponse);
	}

	@Transactional
	public BookingResponse cancel(UUID bookingId) {
		Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("booking_not_found"));
		UUID userId = SecurityUtil.currentUserId();
		if (!booking.getUserId().equals(userId) && !SecurityUtil.hasRole("ROLE_PLATFORM_ADMIN")) {
			throw new BadRequestException("not_allowed");
		}
		if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
			throw new BadRequestException("booking_not_cancellable");
		}

		releaseSeatsWithOptimisticLock(booking.getEventId(), booking.getSeatCategory(), booking.getQuantity());
		booking.setStatus(BookingStatus.CANCELLED);
		Booking saved = bookingRepository.save(booking);
		return toResponse(saved);
	}

	private BookingResponse toResponse(Booking booking) {
		return new BookingResponse(
				booking.getId(),
				booking.getEventId(),
				booking.getUserId(),
				booking.getSeatCategory(),
				booking.getQuantity(),
				booking.getTotalAmountPaise(),
				booking.getCurrency(),
				booking.getStatus(),
				booking.getCreatedAt()
		);
	}

	private void ensureParticipantMembership(UUID eventId, UUID userId) {
		Optional<EventMembership> existing = membershipRepository.findByEventIdAndUserId(eventId, userId);
		if (existing.isPresent()) {
			return;
		}
		EventMembership m = new EventMembership();
		m.setId(UUID.randomUUID());
		m.setEventId(eventId);
		m.setUserId(userId);
		m.setRole(EventMembershipRole.PARTICIPANT);
		membershipRepository.save(m);
	}

	private SeatInventory reserveSeatsWithOptimisticLock(UUID eventId, String category, int qty) {
		int attempts = 0;
		while (true) {
			attempts++;
			try {
				SeatInventory inventory = seatInventoryRepository.findByEventIdAndCategory(eventId, category)
						.orElseThrow(() -> new NotFoundException("seat_category_not_found"));
				if (qty <= 0) {
					throw new BadRequestException("invalid_quantity");
				}
				if (inventory.getAvailableCount() < qty) {
					throw new BadRequestException("insufficient_seats");
				}
				inventory.setAvailableCount(inventory.getAvailableCount() - qty);
				SeatInventory saved = seatInventoryRepository.save(inventory);
				hotAvailabilityService.putAvailable(eventId, saved.getCategory(), saved.getAvailableCount());
				return saved;
			} catch (OptimisticLockingFailureException ex) {
				if (attempts >= 6) {
					throw new BadRequestException("seat_selection_conflict_retry");
				}
			}
		}
	}

	private void releaseSeatsWithOptimisticLock(UUID eventId, String category, int qty) {
		int attempts = 0;
		while (true) {
			attempts++;
			try {
				SeatInventory inventory = seatInventoryRepository.findByEventIdAndCategory(eventId, category)
						.orElseThrow(() -> new NotFoundException("seat_category_not_found"));
				inventory.setAvailableCount(Math.min(inventory.getTotalCount(), inventory.getAvailableCount() + qty));
				SeatInventory saved = seatInventoryRepository.save(inventory);
				hotAvailabilityService.putAvailable(eventId, saved.getCategory(), saved.getAvailableCount());
				return;
			} catch (OptimisticLockingFailureException ex) {
				if (attempts >= 6) {
					throw new BadRequestException("seat_release_conflict_retry");
				}
			}
		}
	}
}
