package com.evenlo.service;

import com.evenlo.dto.event.EventCreateRequest;
import com.evenlo.dto.event.EventResponse;
import com.evenlo.dto.event.EventUpdateRequest;
import com.evenlo.dto.event.SeatCategoryRequest;
import com.evenlo.dto.event.SeatCategoryResponse;
import com.evenlo.exception.BadRequestException;
import com.evenlo.exception.ForbiddenException;
import com.evenlo.exception.NotFoundException;
import com.evenlo.model.Event;
import com.evenlo.model.EventMembership;
import com.evenlo.model.EventMembershipRole;
import com.evenlo.model.EventStatus;
import com.evenlo.model.SeatInventory;
import com.evenlo.repository.EventMembershipRepository;
import com.evenlo.repository.EventRepository;
import com.evenlo.repository.SeatInventoryRepository;
import com.evenlo.util.EventSpecifications;
import com.evenlo.util.PageResponse;
import com.evenlo.util.SecurityUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {
	private final EventRepository eventRepository;
	private final SeatInventoryRepository seatInventoryRepository;
	private final EventMembershipRepository membershipRepository;
	private final HotAvailabilityService hotAvailabilityService;

	public EventService(
			EventRepository eventRepository,
			SeatInventoryRepository seatInventoryRepository,
			EventMembershipRepository membershipRepository,
			HotAvailabilityService hotAvailabilityService
	) {
		this.eventRepository = eventRepository;
		this.seatInventoryRepository = seatInventoryRepository;
		this.membershipRepository = membershipRepository;
		this.hotAvailabilityService = hotAvailabilityService;
	}

	@Transactional
	public EventResponse create(EventCreateRequest request) {
		UUID userId = SecurityUtil.currentUserId();
		validateTimes(request.startsAt(), request.endsAt());

		Event event = new Event();
		event.setId(UUID.randomUUID());
		event.setTitle(request.title().trim());
		event.setDescription(request.description().trim());
		event.setCity(request.city().trim());
		event.setVenueName(request.venueName().trim());
		event.setVenueAddress(request.venueAddress().trim());
		event.setStartsAt(request.startsAt());
		event.setEndsAt(request.endsAt());
		event.setStatus(EventStatus.DRAFT);
		event.setCreatedBy(userId);

		Event saved = eventRepository.save(event);

		EventMembership hostMembership = new EventMembership();
		hostMembership.setId(UUID.randomUUID());
		hostMembership.setEventId(saved.getId());
		hostMembership.setUserId(userId);
		hostMembership.setRole(EventMembershipRole.HOST);
		membershipRepository.save(hostMembership);

		List<SeatCategoryResponse> categories = new ArrayList<>();
		for (SeatCategoryRequest c : request.seatCategories()) {
			SeatInventory inv = new SeatInventory();
			inv.setId(UUID.randomUUID());
			inv.setEventId(saved.getId());
			inv.setCategory(c.category().trim());
			inv.setPricePerSeatPaise(c.pricePerSeatPaise());
			inv.setTotalCount(c.totalCount());
			inv.setAvailableCount(c.totalCount());
			SeatInventory created = seatInventoryRepository.save(inv);
			hotAvailabilityService.putAvailable(saved.getId(), created.getCategory(), created.getAvailableCount());
			categories.add(new SeatCategoryResponse(created.getCategory(), created.getPricePerSeatPaise(), created.getTotalCount(), created.getAvailableCount()));
		}

		return toResponse(saved, categories);
	}

	@Transactional(readOnly = true)
	public EventResponse get(UUID id) {
		Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("event_not_found"));
		List<SeatCategoryResponse> categories = seatInventoryRepository.findAllByEventId(id).stream()
				.map(inv -> {
					int available = hotAvailabilityService.getAvailable(id, inv.getCategory()).orElse(inv.getAvailableCount());
					return new SeatCategoryResponse(inv.getCategory(), inv.getPricePerSeatPaise(), inv.getTotalCount(), available);
				})
				.toList();
		return toResponse(event, categories);
	}

	@Transactional(readOnly = true)
	public PageResponse<EventResponse> list(Optional<String> city, Optional<EventStatus> status, Optional<Instant> startsAfter, Optional<Instant> startsBefore, Pageable pageable) {
		Specification<Event> spec = (root, query, cb) -> null;
		if (city.isPresent() && !city.get().isBlank()) {
			spec = spec.and(EventSpecifications.cityEquals(city.get().trim()));
		}
		if (status.isPresent()) {
			spec = spec.and(EventSpecifications.statusEquals(status.get()));
		}
		if (startsAfter.isPresent()) {
			spec = spec.and(EventSpecifications.startsAtGte(startsAfter.get()));
		}
		if (startsBefore.isPresent()) {
			spec = spec.and(EventSpecifications.startsAtLte(startsBefore.get()));
		}

		Page<Event> page = eventRepository.findAll(spec, pageable);
		List<EventResponse> items = page.getContent().stream()
				.map(e -> new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getCity(), e.getVenueName(), e.getVenueAddress(), e.getStartsAt(), e.getEndsAt(), e.getStatus(), e.getCreatedBy(), e.getImageFileId(), List.of()))
				.toList();

		return new PageResponse<>(items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
	}

	@Transactional
	public EventResponse update(UUID id, EventUpdateRequest request) {
		UUID userId = SecurityUtil.currentUserId();
		Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("event_not_found"));
		requireEventAdminOrHost(event.getId(), userId);
		validateTimes(request.startsAt(), request.endsAt());

		event.setTitle(request.title().trim());
		event.setDescription(request.description().trim());
		event.setCity(request.city().trim());
		event.setVenueName(request.venueName().trim());
		event.setVenueAddress(request.venueAddress().trim());
		event.setStartsAt(request.startsAt());
		event.setEndsAt(request.endsAt());

		Event saved = eventRepository.save(event);
		List<SeatCategoryResponse> categories = seatInventoryRepository.findAllByEventId(id).stream()
				.map(inv -> new SeatCategoryResponse(inv.getCategory(), inv.getPricePerSeatPaise(), inv.getTotalCount(), inv.getAvailableCount()))
				.toList();
		return toResponse(saved, categories);
	}

	@Transactional
	public void publish(UUID id) {
		UUID userId = SecurityUtil.currentUserId();
		Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("event_not_found"));
		requireEventAdminOrHost(event.getId(), userId);
		event.setStatus(EventStatus.PUBLISHED);
		eventRepository.save(event);
	}

	@Transactional
	public void delete(UUID id) {
		UUID userId = SecurityUtil.currentUserId();
		Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("event_not_found"));
		requireEventAdminOrHost(event.getId(), userId);
		eventRepository.delete(event);
	}

	@Transactional
	public void assignEventAdmin(UUID eventId, UUID targetUserId) {
		UUID actorUserId = SecurityUtil.currentUserId();
		requireEventAdminOrHost(eventId, actorUserId);

		EventMembership membership = membershipRepository.findByEventIdAndUserId(eventId, targetUserId).orElseGet(() -> {
			EventMembership m = new EventMembership();
			m.setId(UUID.randomUUID());
			m.setEventId(eventId);
			m.setUserId(targetUserId);
			m.setRole(EventMembershipRole.PARTICIPANT);
			return m;
		});

		membership.setRole(EventMembershipRole.EVENT_ADMIN);
		membershipRepository.save(membership);
	}

	@Transactional
	public void setEventImage(UUID eventId, UUID fileId) {
		UUID actorUserId = SecurityUtil.currentUserId();
		Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("event_not_found"));
		requireEventAdminOrHost(eventId, actorUserId);
		event.setImageFileId(fileId);
		eventRepository.save(event);
	}

	public void requireEventAdminOrHost(UUID eventId, UUID userId) {
		if (SecurityUtil.hasRole("ROLE_PLATFORM_ADMIN")) {
			return;
		}
		boolean ok = membershipRepository.existsByEventIdAndUserIdAndRoleIn(
				eventId,
				userId,
				List.of(EventMembershipRole.HOST, EventMembershipRole.EVENT_ADMIN)
		);
		if (!ok) {
			throw new ForbiddenException("forbidden");
		}
	}

	private void validateTimes(Instant startsAt, Instant endsAt) {
		if (!endsAt.isAfter(startsAt)) {
			throw new BadRequestException("endsAt_must_be_after_startsAt");
		}
	}

	private EventResponse toResponse(Event event, List<SeatCategoryResponse> seatCategories) {
		return new EventResponse(
				event.getId(),
				event.getTitle(),
				event.getDescription(),
				event.getCity(),
				event.getVenueName(),
				event.getVenueAddress(),
				event.getStartsAt(),
				event.getEndsAt(),
				event.getStatus(),
				event.getCreatedBy(),
				event.getImageFileId(),
				seatCategories
		);
	}
}
