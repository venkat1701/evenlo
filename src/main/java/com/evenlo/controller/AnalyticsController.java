package com.evenlo.controller;

import com.evenlo.dto.analytics.EventSummaryResponse;
import com.evenlo.dto.analytics.TopEventResponse;
import com.evenlo.service.AnalyticsService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/top-events")
	public List<TopEventResponse> topEvents(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(defaultValue = "10") int limit
	) {
		return analyticsService.topEvents(from, to, limit);
	}

	@GetMapping("/events/{eventId}/summary")
	public EventSummaryResponse eventSummary(@PathVariable UUID eventId) {
		return analyticsService.eventSummary(eventId);
	}
}
