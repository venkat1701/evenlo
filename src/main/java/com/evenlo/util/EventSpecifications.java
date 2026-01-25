package com.evenlo.util;

import com.evenlo.model.Event;
import com.evenlo.model.EventStatus;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public final class EventSpecifications {
	private EventSpecifications() {
	}

	public static Specification<Event> cityEquals(String city) {
		return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), city.toLowerCase());
	}

	public static Specification<Event> statusEquals(EventStatus status) {
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<Event> startsAtGte(Instant startsAt) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startsAt"), startsAt);
	}

	public static Specification<Event> startsAtLte(Instant startsAt) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startsAt"), startsAt);
	}
}
