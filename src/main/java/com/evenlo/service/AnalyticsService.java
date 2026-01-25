package com.evenlo.service;

import com.evenlo.dto.analytics.EventSummaryResponse;
import com.evenlo.dto.analytics.TopEventResponse;
import com.evenlo.exception.NotFoundException;
import com.evenlo.repository.AnalyticsRepository;
import com.evenlo.util.SecurityUtil;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
	private final AnalyticsRepository analyticsRepository;
	private final EventService eventService;

	public AnalyticsService(AnalyticsRepository analyticsRepository, EventService eventService) {
		this.analyticsRepository = analyticsRepository;
		this.eventService = eventService;
	}

	@Transactional(readOnly = true)
	public List<TopEventResponse> topEvents(Instant from, Instant to, int limit) {
		if (!SecurityUtil.hasRole("ROLE_PLATFORM_ADMIN")) {
			throw new com.evenlo.exception.ForbiddenException("forbidden");
		}
		return analyticsRepository.topEvents(from, to, limit).stream()
				.map(r -> new TopEventResponse(r.getEventId(), r.getTitle(), r.getBookingsCount(), r.getRevenuePaise()))
				.toList();
	}

	@Transactional(readOnly = true)
	public EventSummaryResponse eventSummary(UUID eventId) {
		UUID actor = SecurityUtil.currentUserId();
		if (!SecurityUtil.hasRole("ROLE_PLATFORM_ADMIN")) {
			eventService.requireEventAdminOrHost(eventId, actor);
		}
		AnalyticsRepository.EventSummaryRow row = analyticsRepository.eventSummary(eventId);
		if (row == null) {
			throw new NotFoundException("no_data");
		}
		return new EventSummaryResponse(row.getEventId(), row.getTotalBookings(), row.getConfirmedBookings(), row.getSeatsSold(), row.getRevenuePaise());
	}
}
