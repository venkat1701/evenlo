package com.evenlo.controller;

import com.evenlo.dto.event.EventCreateRequest;
import com.evenlo.dto.event.EventResponse;
import com.evenlo.dto.event.EventUpdateRequest;
import com.evenlo.model.EventStatus;
import com.evenlo.service.EventService;
import com.evenlo.util.PageResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@PostMapping
	public EventResponse create(@Valid @RequestBody EventCreateRequest request) {
		return eventService.create(request);
	}

	@GetMapping("/{id}")
	public EventResponse get(@PathVariable UUID id) {
		return eventService.get(id);
	}

	@GetMapping
	public PageResponse<EventResponse> list(
			@RequestParam Optional<String> city,
			@RequestParam Optional<EventStatus> status,
			@RequestParam(name = "startsAfter") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<Instant> startsAfter,
			@RequestParam(name = "startsBefore") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<Instant> startsBefore,
			Pageable pageable
	) {
		return eventService.list(city, status, startsAfter, startsBefore, pageable);
	}

	@PutMapping("/{id}")
	public EventResponse update(@PathVariable UUID id, @Valid @RequestBody EventUpdateRequest request) {
		return eventService.update(id, request);
	}

	@PostMapping("/{id}/publish")
	public void publish(@PathVariable UUID id) {
		eventService.publish(id);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable UUID id) {
		eventService.delete(id);
	}

	@PostMapping("/{eventId}/admins/{userId}")
	public void assignEventAdmin(@PathVariable UUID eventId, @PathVariable UUID userId) {
		eventService.assignEventAdmin(eventId, userId);
	}

	@PostMapping("/{eventId}/image/{fileId}")
	public void setImage(@PathVariable UUID eventId, @PathVariable UUID fileId) {
		eventService.setEventImage(eventId, fileId);
	}
}
